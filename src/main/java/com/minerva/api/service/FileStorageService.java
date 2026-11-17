package com.minerva.api.service;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.dto.FileDTO;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    private static final String ORIGINAL_FILENAME_METADATA = "original-filename-b64";

    private final S3Client s3Client;
    private final String bucket;

    public FileStorageService(
            S3Client s3Client,
            @Value("${app.storage.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public FileDTO upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un archivo PDF.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(413), "El tamaño máximo permitido es 10 MiB.");
        }

        String originalName = file.getOriginalFilename() == null ? "document.pdf" : file.getOriginalFilename();
        if (!originalName.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
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

        UUID fileId = UUID.randomUUID();
        String objectKey = objectKey(fileId);
        String encodedName = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(originalName.getBytes(java.nio.charset.StandardCharsets.UTF_8));

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
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo guardar el archivo en el almacenamiento S3.");
        }

        return new FileDTO(fileId, originalName, "application/pdf", content.length);
    }

    public StoredFile download(UUID fileId) {
        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey(fileId))
                            .build());

            String encodedName = response.response().metadata().get(ORIGINAL_FILENAME_METADATA);
            String fileName = decodeFileName(encodedName, fileId);
            return new StoredFile(fileName, response.asByteArray());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el archivo.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo recuperar el archivo del almacenamiento S3.");
        }
    }

    private static String objectKey(UUID fileId) {
        return "test/" + fileId + ".pdf";
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

    private static String decodeFileName(String encodedName, UUID fileId) {
        if (encodedName == null) {
            return fileId + ".pdf";
        }
        try {
            return new String(Base64.getUrlDecoder().decode(encodedName), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return fileId + ".pdf";
        }
    }

    public record StoredFile(String fileName, byte[] content) {
    }
}