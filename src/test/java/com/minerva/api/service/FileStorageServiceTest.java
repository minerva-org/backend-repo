package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.dto.DocumentoDTO;
import com.minerva.api.dto.FileDTO;
import com.minerva.api.model.Documento;
import com.minerva.api.repository.DocumentoRepository;
import com.minerva.api.repository.FuentesGrupoRepository;
import com.minerva.api.repository.MateriaRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock private S3Client s3Client;
    @Mock private DocumentoService documentoService;
    @Mock private DocumentoRepository documentoRepository;
    @Mock private MateriaRepository materiaRepository;
    @Mock private FuentesGrupoRepository fuentesGrupoRepository;

    private FileStorageService service;

    @BeforeEach
    void setUp() {
        service = new FileStorageService(
            s3Client,
            documentoService,
            documentoRepository,
            materiaRepository,
            fuentesGrupoRepository,
            "test-bucket",
            "documentos/");
    }

    // Input: archivo null.
    // Resultado esperado: BAD_REQUEST con instrucción concreta y sin llamar a S3.
    @Test
    void upload_shouldRejectNullFile() {
        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> service.upload(null, "PERSONA-1"));

        assertEquals(400, error.getStatusCode().value());
        assertTrue(error.getReason().contains("PDF"));
        verifyNoS3();
    }

    // Input: archivo vacío.
    // Resultado esperado: BAD_REQUEST y sin persistencia.
    @Test
    void upload_shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "plan.pdf", "application/pdf", new byte[0]);

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> service.upload(file, "PERSONA-1"));

        assertEquals(400, error.getStatusCode().value());
        verifyNoS3();
    }

    // Input: archivo PDF de 10 MiB + 1 byte.
    // Resultado esperado: 413 y sin escribir en S3.
    @Test
    void upload_shouldRejectFileOverTenMiB() {
        byte[] content = new byte[10 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile(
            "file", "plan.pdf", "application/pdf", content);

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> service.upload(file, "PERSONA-1"));

        assertEquals(413, error.getStatusCode().value());
        assertTrue(error.getReason().contains("10 MiB"));
        verifyNoS3();
    }

    // Input: nombre sin extensión .pdf.
    // Resultado esperado: 415 y sin escribir en S3.
    @Test
    void upload_shouldRejectNonPdfName() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "plan.txt", "application/pdf", "%PDF-1.4".getBytes());

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> service.upload(file, "PERSONA-1"));

        assertEquals(415, error.getStatusCode().value());
        verifyNoS3();
    }

    // Input: archivo .pdf cuyo contenido no inicia con %PDF-.
    // Resultado esperado: 415 y sin persistencia.
    @Test
    void upload_shouldRejectInvalidPdfSignature() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "plan.pdf", "application/pdf", "no-es-pdf".getBytes());

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> service.upload(file, "PERSONA-1"));

        assertEquals(415, error.getStatusCode().value());
        verifyNoS3();
    }

    // Input: PDF válido ya registrado por checksum.
    // Resultado esperado: reutiliza el Documento existente y no escribe S3.
    @Test
    void upload_shouldReuseDocumentByChecksum() {
        byte[] content = "%PDF-1.4 duplicated".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "otro-nombre.pdf", "application/pdf", content);
        Documento documento = new Documento();
        documento.setId(UUID.randomUUID().toString());
        documento.setOriginalFilename("plan-original.pdf");
        documento.setContentType("application/pdf");
        documento.setSizeBytes(content.length);

        when(documentoRepository.findFirstByChecksumSha256OrderByCreatedAtDesc(any())).thenReturn(Optional.of(documento));

        FileDTO result = service.upload(file, "PERSONA-1");

        assertEquals(documento.getId(), result.fileId().toString());
        assertEquals("plan-original.pdf", result.fileName());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    // Input: PDF válido nuevo y guardado exitosamente.
    // Resultado esperado: escribe en documentos/<uuid>.pdf y devuelve el ID persistido.
    @Test
    void upload_shouldPersistPdfAndReturnSavedId() {
        byte[] content = "%PDF-1.4 nuevo".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "plan.pdf", "application/pdf", content);
        String documentoId = UUID.randomUUID().toString();
        DocumentoDTO guardado = new DocumentoDTO(
            documentoId, "documentos/" + UUID.randomUUID() + ".pdf",
            "plan.pdf", "application/pdf", content.length,
            "checksum", "PERSONA-1", null);

        when(documentoRepository.findFirstByChecksumSha256OrderByCreatedAtDesc(any())).thenReturn(Optional.empty());
        when(documentoService.saveDocumento(any(DocumentoDTO.class))).thenReturn(guardado);

        FileDTO result = service.upload(file, "PERSONA-1");

        assertEquals(documentoId, result.fileId().toString());
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    // Input: PDF nuevo cuyo registro en BD falla.
    // Resultado esperado: elimina el objeto recién subido y propaga el error.
    @Test
    void upload_shouldDeleteS3ObjectWhenDatabaseSaveFails() {
        byte[] content = "%PDF-1.4 rollback".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "plan.pdf", "application/pdf", content);

        when(documentoRepository.findFirstByChecksumSha256OrderByCreatedAtDesc(any())).thenReturn(Optional.empty());
        when(documentoService.saveDocumento(any(DocumentoDTO.class)))
            .thenThrow(new IllegalArgumentException("No se pudo guardar el documento"));

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> service.upload(file, "PERSONA-1"));

        assertTrue(error.getMessage().contains("documento"));
        verify(s3Client).deleteObject(any(software.amazon.awssdk.services.s3.model.DeleteObjectRequest.class));
    }

    // Input: Documento huérfano solicitado para eliminación.
    // Resultado esperado: elimina S3 y registro documental.
    @Test
    void deleteIfOrphan_shouldDeleteUnreferencedDocument() {
        UUID id = UUID.randomUUID();
        Documento documento = new Documento();
        documento.setId(id.toString());
        documento.setObjectKey("documentos/huérfano.pdf");

        when(documentoRepository.findById(id.toString())).thenReturn(Optional.of(documento));
        when(materiaRepository.existsByDocumentoMateria_Id(id.toString())).thenReturn(false);
        when(fuentesGrupoRepository.existsByDocumento_Id(id.toString())).thenReturn(false);

        service.deleteIfOrphan(id);

        verify(s3Client).deleteObject(any(software.amazon.awssdk.services.s3.model.DeleteObjectRequest.class));
        verify(documentoRepository).delete(documento);
    }

    // Input: Documento asociado a materia.
    // Resultado esperado: no elimina S3 ni registro documental.
    @Test
    void deleteIfOrphan_shouldKeepReferencedDocument() {
        UUID id = UUID.randomUUID();
        Documento documento = new Documento();
        documento.setId(id.toString());
        documento.setObjectKey("documentos/uso.pdf");

        when(documentoRepository.findById(id.toString())).thenReturn(Optional.of(documento));
        when(materiaRepository.existsByDocumentoMateria_Id(id.toString())).thenReturn(true);

        service.deleteIfOrphan(id);

        verify(s3Client, never()).deleteObject(any(software.amazon.awssdk.services.s3.model.DeleteObjectRequest.class));
        verify(documentoRepository, never()).delete(any());
    }

    private void verifyNoS3() {
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(s3Client, never()).deleteObject(any(software.amazon.awssdk.services.s3.model.DeleteObjectRequest.class));
    }
}
