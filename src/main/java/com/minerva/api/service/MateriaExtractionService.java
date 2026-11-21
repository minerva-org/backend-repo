package com.minerva.api.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.User.Roles;
import com.minerva.api.User.User;

import tools.jackson.databind.JsonNode;

@Service
public class MateriaExtractionService {
    private static final Logger LOGGER =
            Logger.getLogger(MateriaExtractionService.class.getName());

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final byte[] PDF_SIGNATURE =
            "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final int CACHE_LIMIT = 256;

    private final AiLaboratoryClient aiLaboratoryClient;
    private final Map<String, JsonNode> extractionCache =
            new LinkedHashMap<>(CACHE_LIMIT, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, JsonNode> eldest) {
                    return size() > CACHE_LIMIT;
                }
            };

    public MateriaExtractionService(AiLaboratoryClient aiLaboratoryClient) {
        this.aiLaboratoryClient = aiLaboratoryClient;
    }

    public ResponseEntity<JsonNode> extract(MultipartFile file, User usuario) {
        validarRol(usuario);
        byte[] content = validarPdf(file);
        String checksum = sha256Hex(content);

        synchronized (extractionCache) {
            JsonNode cached = extractionCache.get(checksum);
            if (cached != null) {
                LOGGER.log(Level.INFO,
                        "Extraccion reutilizada por checksum: checksum={0}, rol={1}",
                        new Object[]{checksumPrefix(checksum), usuario.getPersona().getRol()});
                return ResponseEntity.ok(cached);
            }
        }

        LOGGER.log(Level.INFO,
                "Enviando PDF a AI-Service: checksum={0}, archivo={1}, bytes={2}, rol={3}",
                new Object[]{
                        checksumPrefix(checksum),
                        file.getOriginalFilename(),
                        file.getSize(),
                        usuario.getPersona().getRol()
                });
        ResponseEntity<JsonNode> response;
        try {
            response = aiLaboratoryClient.extractDocument(file);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING,
                    "Fallo al contactar AI-Service: checksum={0}, error={1}",
                    new Object[]{checksumPrefix(checksum), exception.getClass().getSimpleName()});
            throw exception;
        }
        LOGGER.log(Level.INFO,
                "Respuesta de extraccion: checksum={0}, status={1}, rol={2}",
                new Object[]{checksumPrefix(checksum), response.getStatusCode().value(),
                        usuario.getPersona().getRol()});
        if (response.getStatusCode().value() == 200) {
            JsonNode tree = response.getBody();
            List<String> errors = validateTree(tree);
            if (!errors.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_CONTENT,
                        "El archivo no contiene una jerarquia valida.",
                        new IllegalArgumentException(String.join(" | ", errors)));
            }
            synchronized (extractionCache) {
                extractionCache.put(checksum, tree);
            }
        }
        return response;
    }

    private void validarRol(User usuario) {
        if (usuario == null || usuario.getPersona() == null || usuario.getPersona().getRol() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para extraer el contenido de materias.");
        }
        Roles rol = usuario.getPersona().getRol();
        if (rol != Roles.ADMIN
                && rol != Roles.DEV
                && rol != Roles.DIRECTOR_GENERAL
                && rol != Roles.DIRECTOR_PLANTEL
                && rol != Roles.COORDINADOR) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para extraer el contenido de materias.");
        }
    }

    private byte[] validarPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecciona un archivo PDF.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.valueOf(413),
                    "El archivo supera el limite permitido de 10 MiB.");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null
                || !originalName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Solo se permiten archivos PDF.");
        }

        final byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se pudo leer el archivo.",
                    exception);
        }
        if (!hasPdfSignature(content)) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "El contenido no parece ser un PDF valido.");
        }
        return content;
    }

    private boolean hasPdfSignature(byte[] content) {
        if (content == null || content.length < PDF_SIGNATURE.length) {
            return false;
        }
        for (int offset = 0; offset <= content.length - PDF_SIGNATURE.length; offset++) {
            boolean matches = true;
            for (int index = 0; index < PDF_SIGNATURE.length; index++) {
                if (content[offset + index] != PDF_SIGNATURE[index]) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return true;
            }
        }
        return false;
    }

    private List<String> validateTree(JsonNode tree) {
        List<String> errors = new ArrayList<>();
        if (tree == null || !tree.isObject()) {
            return List.of("La respuesta no contiene un objeto JSON.");
        }
        JsonNode units = tree.get("units");
        if (units == null || !units.isArray() || units.isEmpty()) {
            return List.of("No se identifico una jerarquia academica utilizable.");
        }
        for (int unitIndex = 0; unitIndex < units.size(); unitIndex++) {
            JsonNode unit = units.get(unitIndex);
            String unitPath = "$.units[" + unitIndex + "]";
            if (!unit.isObject() || !validName(unit.get("name"))) {
                errors.add(unitPath + ".name: se requiere texto no vacio.");
            }
            JsonNode topics = unit.get("topics");
            if (topics == null || !topics.isArray() || topics.isEmpty()) {
                errors.add(unitPath + ".topics: se requiere al menos un tema.");
                continue;
            }
            for (int topicIndex = 0; topicIndex < topics.size(); topicIndex++) {
                JsonNode topic = topics.get(topicIndex);
                String topicPath = unitPath + ".topics[" + topicIndex + "]";
                if (!topic.isObject() || !validName(topic.get("name"))) {
                    errors.add(topicPath + ".name: se requiere texto no vacio.");
                }
                JsonNode concepts = topic.get("concepts");
                if (concepts == null || !concepts.isArray() || concepts.isEmpty()) {
                    errors.add(topicPath + ".concepts: se requiere al menos un concepto.");
                    continue;
                }
                for (int conceptIndex = 0; conceptIndex < concepts.size(); conceptIndex++) {
                    JsonNode concept = concepts.get(conceptIndex);
                    if (!concept.isObject()
                            || !validName(concept.get("name"))) {
                        errors.add(topicPath + ".concepts[" + conceptIndex
                                + "].name: se requiere texto no vacio.");
                    }
                }
            }
        }
        return errors;
    }

    private boolean validName(JsonNode name) {
        return name != null && name.isString() && !name.asString().isBlank();
    }

    private String sha256Hex(byte[] content) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no disponible", exception);
        }
    }

    private String checksumPrefix(String checksum) {
        return checksum.substring(0, Math.min(12, checksum.length()));
    }
}
