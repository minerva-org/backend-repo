package com.minerva.api.service;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.User.Roles;
import com.minerva.api.model.Documento;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Persona;
import com.minerva.api.repository.FuentesGrupoRepository;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.PersonaRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Service
@RequiredArgsConstructor
public class CurriculoIaServiceImpl implements CurriculoIaService {
    private final GrupoRepository grupoRepository;
    private final PersonaRepository personaRepository;
    private final FuentesGrupoRepository fuentesGrupoRepository;
    private final FileStorageService fileStorageService;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;

    @Value("${app.curriculo.mock-enabled:false}")
    private boolean mockEnabled;

    private final Map<String, MockCurriculumTask> mockTasks =
            new ConcurrentHashMap<>();

    @Override
    public JsonNode generar(
            String grupoId,
            JsonNode request,
            String personaId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Grupo no encontrado."));
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Persona no encontrada."));
        validarPermisoGeneracion(grupo, persona);

        if (request == null
                || !request.isObject()
                || request.get("arbol") == null
                || !request.get("arbol").isObject()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "El arbol curricular es obligatorio.");
        }

        if (mockEnabled) {
            String tareaId = UUID.randomUUID().toString();
            mockTasks.put(tareaId, new MockCurriculumTask(
                    0,
                    buildMockCurriculum(request.get("arbol"))));
            ObjectNode response = objectMapper.createObjectNode();
            response.put("tareaId", tareaId);
            return response;
        }

        ObjectNode taskData = objectMapper.createObjectNode();
        taskData.put("grupoId", grupoId);
        String materiaNombre = text(request, "materiaNombre");
        if (materiaNombre.isBlank()) {
            JsonNode root = request.get("arbol");
            materiaNombre = root != null && root.isObject()
                    ? text(root, "nombre")
                    : "";
        }
        if (materiaNombre.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "El nombre de la materia es obligatorio.");
        }
        taskData.put("materiaNombre", materiaNombre);
        taskData.set("arbol", request.get("arbol"));
        taskData.put("modoFusion",
                text(request, "modoFusion").isBlank()
                        ? "conservadora"
                        : text(request, "modoFusion"));

        ArrayNode taskSources = taskData.putArray("fuentes");
        ArrayNode processedSources = taskData.putArray("fuentesProcesadas");
        ArrayNode files = taskData.putArray("archivos");

        JsonNode processed = request.get("fuentesProcesadas");
        if (processed != null && processed.isArray()) {
            for (JsonNode item : processed) {
                processedSources.add(item);
            }
        }

        JsonNode sources = request.get("fuentes");
        if (sources != null && sources.isArray()) {
            for (JsonNode source : sources) {
                String documentoId = text(source, "documentoId");
                if (documentoId.isBlank()) {
                    continue;
                }
                if (containsSource(processed, documentoId)) {
                    continue;
                }

                boolean associated = fuentesGrupoRepository
                        .existsById(new com.minerva.api.model.FuentesGrupoId(
                                documentoId,
                                grupoId));
                if (!associated) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Una fuente no pertenece al grupo.");
                }

                Documento documento = fuentesGrupoRepository
                        .findByGrupoIdOrderByDocumentoCreatedAtDesc(grupoId)
                        .stream()
                        .map(fuente -> fuente.getDocumento())
                        .filter(item -> documentoId.equals(item.getId()))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Fuente no encontrada."));

                taskSources.add(source);
                FileStorageService.StoredFile stored =
                        fileStorageService.download(
                                java.util.UUID.fromString(documentoId));
                ObjectNode file = files.addObject();
                file.put("nombre", stored.fileName());
                file.put("contenidoBase64",
                        Base64.getEncoder().encodeToString(stored.content()));
            }
        }

        ObjectNode taskRequest = objectMapper.createObjectNode();
        taskRequest.put("tipo", "CURRICULUM_GENERATE");
        taskRequest.set("datos", taskData);
        return aiServiceClient.createTask(taskRequest);
    }

    @Override
    public JsonNode consultarTarea(
            String grupoId,
            String tareaId,
            String personaId) {
        validarAccesoGrupo(grupoId, personaId);
        if (mockEnabled) {
            return consultarTareaMock(grupoId, tareaId);
        }
        return aiServiceClient.getTask(tareaId);
    }

    @Override
    public void cancelarTarea(
            String grupoId,
            String tareaId,
            String personaId) {
        validarAccesoGrupo(grupoId, personaId);
        if (mockEnabled) {
            mockTasks.remove(tareaId);
            return;
        }
        aiServiceClient.deleteTask(tareaId);
    }

    private JsonNode consultarTareaMock(String grupoId, String tareaId) {
        MockCurriculumTask task = mockTasks.get(tareaId);
        if (task == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La tarea mock no existe.");
        }

        int nextPollCount = task.pollCount() + 1;
        mockTasks.put(tareaId,
                new MockCurriculumTask(nextPollCount, task.arbol()));

        ObjectNode status = objectMapper.createObjectNode();
        status.put("tareaId", tareaId);
        status.put("tipo", "CURRICULUM_GENERATE");
        if (nextPollCount < 2) {
            status.put("estado", "EN_COLA");
            status.put("progreso", "Preparando respuesta curricular mock");
            status.putNull("resultado");
            status.putNull("error");
            return status;
        }

        status.put("estado", "COMPLETADA");
        status.put("progreso", "Generacion mock terminada");
        ObjectNode resultado = status.putObject("resultado");
        resultado.put("grupoId", grupoId);
        resultado.put("persistido", false);
        resultado.set("arbol", task.arbol());
        resultado.putArray("fuentesResultado");
        resultado.putArray("fuentesProcesadas");
        resultado.putArray("advertencias");
        resultado.putArray("errores");
        status.putNull("error");
        return status;
    }

    private JsonNode buildMockCurriculum(JsonNode arbol) {
        ObjectNode root = (ObjectNode) objectMapper.valueToTree(arbol);
        JsonNode childrenNode = root.get("hijos");
        ArrayNode children = childrenNode instanceof ArrayNode array
                ? array
                : root.putArray("hijos");

        addContentToFirstExistingConcept(root);
        addThemeToFirstExistingUnit(root);
        addConceptToFirstExistingTheme(root);

        ObjectNode unidad = mockNode(
                "unidad",
                "Métodos de prueba",
                "TEMP-MOCK-UNIT-1");
        ArrayNode temasUnidad = unidad.putArray("hijos");

        ObjectNode tema1 = mockNode(
                "tema",
                "Pruebas unitarias",
                "TEMP-MOCK-THEME-1");
        ArrayNode conceptosTema1 = tema1.putArray("hijos");
        conceptosTema1.add(mockConcept(
                "TEMP-MOCK-CONCEPT-1",
                "JUnit",
                "JUnit permite definir casos de prueba automáticos y verificar resultados esperados."));
        conceptosTema1.add(mockConcept(
                "TEMP-MOCK-CONCEPT-2",
                "Mocking",
                "Los mocks permiten aislar dependencias externas durante una prueba."));
        temasUnidad.add(tema1);

        ObjectNode tema2 = mockNode(
                "tema",
                "Pruebas de integración",
                "TEMP-MOCK-THEME-2");
        ArrayNode conceptosTema2 = tema2.putArray("hijos");
        conceptosTema2.add(mockConcept(
                "TEMP-MOCK-CONCEPT-3",
                "Spring MockMvc",
                "MockMvc permite validar endpoints HTTP sin iniciar el servidor real."));
        temasUnidad.add(tema2);

        children.add(unidad);
        return root;
    }

    private void addContentToFirstExistingConcept(ObjectNode root) {
        ObjectNode firstUnit = firstObject(root, "hijos");
        if (firstUnit == null) {
            return;
        }
        ObjectNode firstTheme = firstObject(firstUnit, "hijos");
        if (firstTheme == null) {
            return;
        }
        ObjectNode firstConcept = firstObject(firstTheme, "hijos");
        if (firstConcept == null) {
            return;
        }

        firstConcept.put("revisionEstado", "modificado");
        firstConcept.put("estadoConcepto", "con_contenido");
        firstConcept.put("dirty", true);
        ArrayNode contenido = firstConcept.get("contenido") instanceof ArrayNode array
                ? array
                : firstConcept.putArray("contenido");
        ObjectNode item = contenido.addObject();
        item.put("id", "TEMP-MOCK-CONTENT-EXISTING");
        item.put("descripcion",
                "Contenido agregado por el mock a un concepto ya existente.");
        item.put("origen", "ia");
        item.put("revisionEstado", "pendiente_revision");
    }

    private void addThemeToFirstExistingUnit(ObjectNode root) {
        ObjectNode firstUnit = firstObject(root, "hijos");
        if (firstUnit == null) {
            return;
        }
        ArrayNode temas = firstUnit.get("hijos") instanceof ArrayNode array
                ? array
                : firstUnit.putArray("hijos");
        temas.add(mockNode(
                "tema",
                "Tema nuevo para unidad existente",
                "TEMP-MOCK-THEME-EXISTING-UNIT"));
    }

    private void addConceptToFirstExistingTheme(ObjectNode root) {
        ObjectNode firstUnit = firstObject(root, "hijos");
        if (firstUnit == null) {
            return;
        }
        ObjectNode firstTheme = firstObject(firstUnit, "hijos");
        if (firstTheme == null) {
            return;
        }
        ArrayNode conceptos = firstTheme.get("hijos") instanceof ArrayNode array
                ? array
                : firstTheme.putArray("hijos");
        conceptos.add(mockConcept(
                "TEMP-MOCK-CONCEPT-EXISTING-THEME",
                "Concepto nuevo para tema existente",
                "Concepto agregado por el mock al tema existente."));
    }

    private ObjectNode firstObject(JsonNode parent, String arrayField) {
        JsonNode children = parent.get(arrayField);
        if (!(children instanceof ArrayNode array)) {
            return null;
        }
        for (JsonNode child : array) {
            if (child instanceof ObjectNode object) {
                return object;
            }
        }
        return null;
    }

    private ObjectNode mockNode(String tipo, String nombre, String id) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", id);
        node.put("tipo", tipo);
        node.put("nombre", nombre);
        node.put("origen", "ia");
        node.put("revisionEstado", "pendiente_revision");
        node.put("dirty", true);
        node.putArray("contenido");
        node.putArray("hijos");
        return node;
    }

    private ObjectNode mockConcept(String id, String nombre, String descripcion) {
        ObjectNode concept = mockNode("concepto", nombre, id);
        concept.put("estadoConcepto", "con_contenido");
        ArrayNode contenido = concept.putArray("contenido");
        ObjectNode item = contenido.addObject();
        item.put("id", "TEMP-MOCK-CONTENT-" + id);
        item.put("descripcion", descripcion);
        item.put("origen", "ia");
        item.put("revisionEstado", "pendiente_revision");
        return concept;
    }

    private void validarPermisoGeneracion(Grupo grupo, Persona persona) {
        Roles role = persona.getRol();
        if (role == Roles.DEV
                || role == Roles.ADMIN
                || role == Roles.DIRECTOR_GENERAL) {
            return;
        }
        if (role == Roles.DOCENTE
                && grupo.getDocente() != null
                && grupo.getDocente().getId().equals(persona.getId())) {
            return;
        }
        if (role == Roles.COORDINADOR
                && grupo.getPlantel() != null
                && persona.getPlantel() != null
                && grupo.getPlantel().getId()
                        .equals(persona.getPlantel().getId())) {
            return;
        }
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No tienes permiso para generar contenido en este grupo.");
    }

    private void validarAccesoGrupo(String grupoId, String personaId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Grupo no encontrado."));
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Persona no encontrada."));
        Roles role = persona.getRol();
        if (role == Roles.DEV
                || role == Roles.ADMIN
                || role == Roles.DIRECTOR_GENERAL) {
            return;
        }
        if (role == Roles.DOCENTE
                && grupo.getDocente() != null
                && grupo.getDocente().getId().equals(persona.getId())) {
            return;
        }
        if ((role == Roles.COORDINADOR
                        || role == Roles.DIRECTOR_PLANTEL)
                && grupo.getPlantel() != null
                && persona.getPlantel() != null
                && grupo.getPlantel().getId()
                        .equals(persona.getPlantel().getId())) {
            return;
        }
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No tienes permiso para acceder a la generacion de este grupo.");
    }

    private record MockCurriculumTask(int pollCount, JsonNode arbol) {
    }

    private boolean containsSource(JsonNode sources, String documentoId) {
        if (sources == null || !sources.isArray()) {
            return false;
        }
        for (JsonNode source : sources) {
            if (documentoId.equals(text(source, "documentoId"))) {
                return true;
            }
        }
        return false;
    }

    private static String text(JsonNode node, String field) {
        if (node == null || !node.isObject()) {
            return "";
        }
        JsonNode value = node.get(field);
        return value != null && value.isString()
                ? value.asString().trim()
                : "";
    }
}
