package com.minerva.api.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.dto.DocumentoDTO;
import com.minerva.api.dto.FileDTO;
import com.minerva.api.model.Documento;
import com.minerva.api.repository.DocumentoRepository;
import com.minerva.api.repository.FuentesGrupoRepository;
import com.minerva.api.repository.MateriaRepository;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class FileStorageService {
    private static final Logger LOGGER = Logger.getLogger(FileStorageService.class.getName());

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final String ORIGINAL_FILENAME_METADATA = "original-filename-b64";
    private static final String DEFAULT_DOCUMENTS_PREFIX = "documentos/";

    private final S3Client s3Client;
    private final DocumentoService documentoService;
    private final DocumentoRepository documentoRepository;
    private final MateriaRepository materiaRepository;
    private final FuentesGrupoRepository fuentesGrupoRepository;
    private final String bucket;
    private final String documentsPrefix;

    public FileStorageService(
            S3Client s3Client,
            DocumentoService documentoService,
            DocumentoRepository documentoRepository,
            MateriaRepository materiaRepository,
            FuentesGrupoRepository fuentesGrupoRepository,
            @Value("${app.storage.s3.bucket}") String bucket,
            @Value("${app.storage.s3.documents-prefix:documentos/}") String documentsPrefix) {
        this.s3Client = s3Client;
        this.documentoService = documentoService;
        this.documentoRepository = documentoRepository;
        this.materiaRepository = materiaRepository;
        this.fuentesGrupoRepository = fuentesGrupoRepository;
        this.bucket = bucket;
        this.documentsPrefix = normalizePrefix(documentsPrefix);
    }

    public FileDTO upload(MultipartFile file, String uploadedBy) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un archivo PDF.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(413), "El tamaño máximo permitido es 10 MiB.");
        }

        String originalName = file.getOriginalFilename() == null ? "document.pdf" : file.getOriginalFilename();
        if (!originalName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Solo se permiten archivos PDF.");
        }

        final byte[] content;
        try {
            content = file.getBytes();
        } catch (java.io.IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo.");
        }
        if (!hasPdfSignature(content)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "El contenido no parece ser un PDF válido.");
        }

        String checksum = sha256Hex(content);
        Optional<Documento> documentoExistente =
                documentoRepository.findFirstByChecksumSha256OrderByCreatedAtDesc(checksum);
        if (documentoExistente.isPresent()) {
            Documento documento = documentoExistente.get();
            return new FileDTO(UUID.fromString(documento.getId()),
                    documento.getOriginalFilename(),
                    documento.getContentType(),
                    documento.getSizeBytes());
        }

        UUID fileId = UUID.randomUUID();
        String objectKey = documentsPrefix + fileId + ".pdf";
        String encodedName = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(originalName.getBytes(StandardCharsets.UTF_8));

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType("application/pdf")
                            .metadata(Map.of(ORIGINAL_FILENAME_METADATA, encodedName))
                            .build(),
                    RequestBody.fromBytes(content));
        } catch (S3Exception exception) {
            logS3Failure("SUBIR", bucket, objectKey, exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo guardar el archivo. Intenta nuevamente.");
        }

        try {
            DocumentoDTO documentoGuardado = documentoService.saveDocumento(new DocumentoDTO(
                    null,
                    objectKey,
                    originalName,
                    "application/pdf",
                    content.length,
                    checksum,
                    uploadedBy,
                    null));
            return new FileDTO(UUID.fromString(documentoGuardado.id()),
                    originalName,
                    "application/pdf",
                    content.length);
        } catch (RuntimeException exception) {
            // por si falla la BD, no dejamos el archivo huérfano en S3
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build());
            } catch (S3Exception rollbackException) {
                logS3Failure("ELIMINAR_ROLLBACK", bucket, objectKey, rollbackException);
            }
            throw exception;
        }

    }

    public StoredFile download(UUID fileId) {
        Documento documento = documentoRepository.findById(fileId.toString())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el documento."));
        return downloadByObjectKey(documento.getObjectKey(),
                documento.getOriginalFilename());
    }

    public StoredFileStream downloadStream(UUID fileId) {
        Documento documento = documentoRepository.findById(fileId.toString())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el documento."));
        try {
            ResponseInputStream<GetObjectResponse> response =
                    s3Client.getObject(GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(documento.getObjectKey())
                            .build());
            return new StoredFileStream(
                    documento.getOriginalFilename(),
                    documento.getSizeBytes(),
                    response);
        } catch (S3Exception exception) {
            logS3Failure(
                    "DESCARGAR_STREAM",
                    bucket,
                    documento.getObjectKey(),
                    exception);
            if (exception.statusCode() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el archivo.");
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo recuperar el archivo. Intenta nuevamente.");
        }
    }

    public void deleteIfOrphan(UUID fileId) {
        Documento documento = documentoRepository.findById(fileId.toString())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el documento."));

        boolean asociado = materiaRepository.existsByDocumentoMateria_Id(fileId.toString())
                || fuentesGrupoRepository.existsByDocumento_Id(fileId.toString());
        if (asociado) {
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(documento.getObjectKey())
                    .build());
        } catch (S3Exception exception) {
            logS3Failure("ELIMINAR", bucket, documento.getObjectKey(), exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo eliminar el archivo. Intenta nuevamente.");
        }
        documentoRepository.delete(documento);
    }

    public StoredFile downloadByReference(String planEstudio, String fuente) {
        String planToken = toComparableToken(planEstudio);
        String fuenteToken = toComparableToken(fuente);
        if (planToken == null && fuenteToken == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debes enviar planEstudio o fuente para buscar el documento.");
        }

        String objectKey = findLatestObjectKeyByReference(planToken, fuenteToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró un documento para los criterios enviados."));

        String fallbackName = fileNameFromObjectKey(objectKey);
        return downloadByObjectKey(objectKey, fallbackName);
    }

    private StoredFile downloadByObjectKey(String objectKey, String fallbackFileName) {
        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build());

            String encodedName = response.response().metadata().get(ORIGINAL_FILENAME_METADATA);
            String fileName = decodeFileName(encodedName, fallbackFileName);
            return new StoredFile(fileName, response.asByteArray());
        } catch (S3Exception exception) {
            logS3Failure("DESCARGAR", bucket, objectKey, exception);
            if (exception.statusCode() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el archivo.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo recuperar el archivo. Intenta nuevamente.");
        }
    }

        private static String sha256Hex(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private void logS3Failure(
            String operacion,
            String bucketName,
            String objectKey,
            S3Exception exception) {
        String awsError = exception.awsErrorDetails() != null
                ? exception.awsErrorDetails().errorCode()
                : "UNKNOWN";
        LOGGER.log(Level.WARNING,
                "Operacion S3 fallida: operacion={0}, bucket={1}, key={2}, awsError={3}, status={4}, requestId={5}",
                new Object[]{
                        operacion,
                        bucketName,
                        objectKey,
                        awsError,
                        exception.statusCode(),
                        exception.requestId()
                });
    }

    private Optional<String> findLatestObjectKeyByReference(String planToken, String fuenteToken) {
        S3Object latestMatch = null;
        String continuationToken = null;

        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(documentsPrefix)
                    .maxKeys(1000);
            if (continuationToken != null) {
                requestBuilder.continuationToken(continuationToken);
            }

            ListObjectsV2Response response;
            try {
                response = s3Client.listObjectsV2(requestBuilder.build());
            } catch (S3Exception exception) {
                logS3Failure("LISTAR", bucket, documentsPrefix, exception);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "No se pudo procesar el archivo. Intenta nuevamente.");
            }

            for (S3Object current : response.contents()) {
                if (!matchesReference(current.key(), planToken, fuenteToken)) {
                    continue;
                }
                if (latestMatch == null || current.lastModified().isAfter(latestMatch.lastModified())) {
                    latestMatch = current;
                }
            }

            continuationToken = response.nextContinuationToken();
            if (!Boolean.TRUE.equals(response.isTruncated())) {
                continuationToken = null;
            }
        } while (continuationToken != null);

        return Optional.ofNullable(latestMatch).map(S3Object::key);
    }


    private static boolean matchesReference(String objectKey, String planToken, String fuenteToken) {
        if (objectKey == null || !objectKey.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            return false;
        }

        String keyToken = toComparableToken(objectKey);
        if (keyToken == null) {
            return false;
        }
        if (planToken != null && !keyToken.contains(planToken)) {
            return false;
        }
        if (fuenteToken != null && !keyToken.contains(fuenteToken)) {
            return false;
        }
        return true;
    }

    private static String normalizePrefix(String prefix) {
        String value = prefix == null || prefix.isBlank() ? DEFAULT_DOCUMENTS_PREFIX : prefix.trim();
        return value.endsWith("/") ? value : value + "/";
    }

    private static String fileNameFromObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return "document.pdf";
        }
        int separatorIndex = objectKey.lastIndexOf('/');
        String filename = separatorIndex >= 0 ? objectKey.substring(separatorIndex + 1) : objectKey;
        return filename.isBlank() ? "document.pdf" : filename;
    }

    private static boolean hasPdfSignature(byte[] content) {
        if (content.length < PDF_SIGNATURE.length) {
            return false;
        }
        for (int index = 0; index < PDF_SIGNATURE.length; index++) {
            if (content[index] != PDF_SIGNATURE[index]) {
                return false;
            }
        }
        return true;
    }

    private static String decodeFileName(String encodedName, String fallbackName) {
        if (encodedName == null) {
            return fallbackName;
        }
        try {
            return new String(Base64.getUrlDecoder().decode(encodedName), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return fallbackName;
        }
    }

    private static String toComparableToken(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
        return normalized.isBlank() ? null : normalized;
    }

    public record StoredFile(String fileName, byte[] content) {
    }

    public record StoredFileStream(
            String fileName,
            long contentLength,
            InputStream content) implements AutoCloseable {

        @Override
        public void close() throws java.io.IOException {
            content.close();
        }
    }
}