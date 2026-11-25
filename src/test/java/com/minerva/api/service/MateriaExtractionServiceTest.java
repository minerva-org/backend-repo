package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.User.Roles;
import com.minerva.api.User.User;
import com.minerva.api.model.Persona;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MateriaExtractionServiceTest {

    @Mock
    private AiLaboratoryClient aiLaboratoryClient;

    private MateriaExtractionService service;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new MateriaExtractionService(aiLaboratoryClient);
    }

    private User usuario(Roles rol) {
        Persona persona = new Persona();
        persona.setRol(rol);
        User usuario = new User();
        usuario.setPersona(persona);
        return usuario;
    }

    // Input: PDF válido con firma %PDF- y rol Director General.
    // Resultado esperado: llama una vez a AI y devuelve el árbol 200.
    @Test
    void extractsValidPdfForAuthorizedRole() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "plan.pdf", "application/pdf",
                "%PDF-1.4 unidad tema concepto".getBytes(StandardCharsets.UTF_8));
        var tree = mapper.readTree(
                "{\"units\":[{\"name\":\"Unidad\",\"topics\":[{\"name\":\"Tema\",\"concepts\":[{\"name\":\"Concepto\"}]}]}]}");
        when(aiLaboratoryClient.extractDocument(file))
                .thenReturn(ResponseEntity.ok(tree));

        ResponseEntity<tools.jackson.databind.JsonNode> response =
                service.extract(file, usuario(Roles.DIRECTOR_GENERAL));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Unidad", response.getBody().at("/units/0/name").asString());
        verify(aiLaboratoryClient).extractDocument(file);
    }

    @Test
    void extractsValidPowerPointForAuthorizedRole() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "plan.pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                new byte[]{0x50, 0x4b, 0x03, 0x04, 1, 2});
        var tree = mapper.readTree(
                "{\"units\":[{\"name\":\"Unidad\",\"topics\":[{\"name\":\"Tema\",\"concepts\":[{\"name\":\"Concepto\"}]}]}]}");
        when(aiLaboratoryClient.extractDocument(file))
                .thenReturn(ResponseEntity.ok(tree));

        ResponseEntity<tools.jackson.databind.JsonNode> response =
                service.extract(file, usuario(Roles.DIRECTOR_GENERAL));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(aiLaboratoryClient).extractDocument(file);
    }

    // Input: mismo PDF válido enviado dos veces.
    // Resultado esperado: reutiliza caché y no llama AI dos veces.
    @Test
    void reusesSuccessfulExtractionByChecksum() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "plan.pdf", "application/pdf",
                "%PDF-1.4 mismo contenido".getBytes(StandardCharsets.UTF_8));
        var tree = mapper.readTree(
                "{\"units\":[{\"name\":\"Unidad\",\"topics\":[{\"name\":\"Tema\",\"concepts\":[{\"name\":\"Concepto\"}]}]}]}");
        when(aiLaboratoryClient.extractDocument(file))
                .thenReturn(ResponseEntity.ok(tree));

        service.extract(file, usuario(Roles.ADMIN));
        service.extract(file, usuario(Roles.ADMIN));

        verify(aiLaboratoryClient, times(1)).extractDocument(file);
    }

    // Input: archivo con nombre .txt.
    // Resultado esperado: 415 y no se invoca AI.
    @Test
    void rejectsNonPdfExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "plan.txt", "application/pdf",
                "%PDF-1.4".getBytes(StandardCharsets.UTF_8));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.extract(file, usuario(Roles.DIRECTOR_GENERAL)));

        assertEquals(415, error.getStatusCode().value());
        verify(aiLaboratoryClient, never()).extractDocument(any());
    }

    // Input: archivo con firma PDF pero nombre vacío.
    // Resultado esperado: 400 y no se invoca AI.
    @Test
    void rejectsEmptyPdf() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "plan.pdf", "application/pdf", new byte[0]);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.extract(file, usuario(Roles.DIRECTOR_GENERAL)));

        assertEquals(400, error.getStatusCode().value());
        verify(aiLaboratoryClient, never()).extractDocument(any());
    }

    // Input: PDF de 10 MiB + 1 byte.
    // Resultado esperado: 413 y no se invoca AI.
    @Test
    void rejectsPdfOverTenMiB() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "plan.pdf", "application/pdf",
                new byte[10 * 1024 * 1024 + 1]);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.extract(file, usuario(Roles.DIRECTOR_GENERAL)));

        assertEquals(413, error.getStatusCode().value());
        verify(aiLaboratoryClient, never()).extractDocument(any());
    }

    // Input: archivo PDF válido con usuario DOCENTE.
    // Resultado esperado: 403 y no se invoca AI.
    @Test
    void rejectsUserWithoutCatalogPermission() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "plan.pdf", "application/pdf",
                "%PDF-1.4".getBytes(StandardCharsets.UTF_8));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.extract(file, usuario(Roles.DOCENTE)));

        assertEquals(403, error.getStatusCode().value());
        verify(aiLaboratoryClient, never()).extractDocument(any());
    }

    // Input: AI devuelve units vacío.
    // Resultado esperado: 422 con mensaje no utilizable.
    @Test
    void rejectsEmptyAiTree() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "plan.pdf", "application/pdf",
                "%PDF-1.4 sin jerarquia".getBytes(StandardCharsets.UTF_8));
        var tree = mapper.readTree("{\"units\":[]}");
        when(aiLaboratoryClient.extractDocument(file))
                .thenReturn(ResponseEntity.ok(tree));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.extract(file, usuario(Roles.DIRECTOR_GENERAL)));

        assertEquals(422, error.getStatusCode().value());
        assertTrue(error.getReason().contains("jerarquia"));
    }

    // Input: AI responde 422 por documento no utilizable.
    // Resultado esperado: conserva el status 422 y el body diagnóstico.
    @Test
    void preservesAi422Response() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "plan.pdf", "application/pdf",
                "%PDF-1.4 no utilizable".getBytes(StandardCharsets.UTF_8));
        var body = mapper.readTree(
                "{\"code\":\"NO_USABLE_HIERARCHY\",\"message\":\"No hay contenido utilizable.\"}");
        when(aiLaboratoryClient.extractDocument(file))
                .thenReturn(ResponseEntity.status(422).body(body));

        ResponseEntity<tools.jackson.databind.JsonNode> response =
                service.extract(file, usuario(Roles.DIRECTOR_GENERAL));

        assertEquals(422, response.getStatusCode().value());
        assertEquals("NO_USABLE_HIERARCHY",
                response.getBody().get("code").asString());
    }

    // Input: AI responde 503 por Ollama no disponible.
    // Resultado esperado: conserva el status 503.
    @Test
    void preservesAi503Response() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "plan.pdf", "application/pdf",
                "%PDF-1.4 ollama caido".getBytes(StandardCharsets.UTF_8));
        var body = mapper.readTree(
                "{\"code\":\"OLLAMA_UNAVAILABLE\",\"message\":\"Servicio no disponible.\"}");
        when(aiLaboratoryClient.extractDocument(file))
                .thenReturn(ResponseEntity.status(503).body(body));

        ResponseEntity<tools.jackson.databind.JsonNode> response =
                service.extract(file, usuario(Roles.DIRECTOR_GENERAL));

        assertEquals(503, response.getStatusCode().value());
        assertEquals("OLLAMA_UNAVAILABLE",
                response.getBody().get("code").asString());
    }
}
