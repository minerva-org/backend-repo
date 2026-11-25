package com.minerva.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.User.User;
import com.minerva.api.User.Roles;
import com.minerva.api.dto.MetricsAssistantRequestDTO;
import com.minerva.api.dto.MetricsAssistantResponseDTO;
import com.minerva.api.dto.AlumnoDesgloseDTO;
import com.minerva.api.dto.EstadisticasDTO.EstadisticasDocentesDTO;
import com.minerva.api.dto.EstadisticasDTO.EstadisticasGrupoResumenDTO;
import com.minerva.api.security.RolAutorizacion;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
@RequiredArgsConstructor
public class MetricsAssistantService {

    private static final Logger LOGGER =
            Logger.getLogger(MetricsAssistantService.class.getName());
    private static final Set<String> SCOPES =
            Set.of("QUIZ", "GRUPO", "PLANTEL", "INSTITUCION");
    private static final Set<String> MESSAGE_ROLES =
            Set.of("user", "assistant");

    private final EstadisticasService estadisticasService;
    private final ResumenVistasService resumenVistasService;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;

    public MetricsAssistantResponseDTO consult(
            MetricsAssistantRequestDTO request,
            User user) {
        String scope = request.scope().trim().toUpperCase(Locale.ROOT);
        if (!SCOPES.contains(scope)) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "Nivel de métricas no válido.");
        }
        authorizeScope(scope, user);

        List<Map<String, String>> messages = buildMessages(request);
        ObjectNode statistics = buildStatistics(scope, request.entityId(), user);
        ObjectNode context = objectMapper.createObjectNode();
        context.put("scope", scope);
        context.put("entityName", statistics.path("entityName").asText());
        context.set("statistics", statistics);
        context.set("messages", objectMapper.valueToTree(messages));

        LOGGER.log(Level.INFO,
                "MINERVA_METRICAS_SOLICITUD: scope={0}, entityId={1}, mensajes={2}",
                new Object[]{scope, request.entityId(), messages.size()});

        JsonNode response = aiServiceClient.consultMetricsAssistant(context);
        JsonNode answer = response.get("answer");
        JsonNode model = response.get("model");
        if (answer == null || !answer.isString() || answer.asString().isBlank()) {
            throw new AiServiceClient.AiServiceUnavailableException(
                    "AI-Service returned an invalid metrics assistant response.");
        }
        return new MetricsAssistantResponseDTO(
                answer.asString(),
                model != null && model.isString() ? model.asString() : "unknown");
    }

    private List<Map<String, String>> buildMessages(
            MetricsAssistantRequestDTO request) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (request.messages() != null) {
            for (MetricsAssistantRequestDTO.MessageDTO message : request.messages()) {
                if (!MESSAGE_ROLES.contains(message.role())) {
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_CONTENT,
                            "Rol de mensaje no válido.");
                }
                messages.add(Map.of(
                        "role", message.role(),
                        "content", message.content()));
            }
        }
        messages.add(Map.of("role", "user", "content", request.prompt().trim()));
        return messages;
    }

    private ObjectNode buildStatistics(
            String scope,
            String entityId,
            User user) {
        return switch (scope) {
            case "QUIZ" -> quizStatistics(requiredId(entityId, scope), user);
            case "GRUPO" -> groupStatistics(requiredId(entityId, scope), user);
            case "PLANTEL" -> plantelStatistics(
                    resolvePlantelId(entityId, scope, user), user);
            case "INSTITUCION" -> institutionStatistics(user);
            default -> throw new IllegalStateException("Unreachable scope");
        };
    }

    private void authorizeScope(String scope, User user) {
        Roles role = RolAutorizacion.obtenerRol(user);
        boolean institutionAccess = role == Roles.ADMIN
                || role == Roles.DEV
                || role == Roles.DIRECTOR_GENERAL;
        boolean campusAccess = institutionAccess
                || role == Roles.DIRECTOR_PLANTEL
                || role == Roles.COORDINADOR;
        boolean detailAccess = campusAccess
                || role == Roles.DOCENTE;

        boolean allowed = switch (scope) {
            case "INSTITUCION" -> institutionAccess;
            case "PLANTEL" -> campusAccess;
            case "GRUPO", "QUIZ" -> detailAccess;
            default -> false;
        };
        if (!allowed) {
            throw new AccessDeniedException(
                    "No tienes permiso para consultar este nivel de métricas.");
        }
    }

    private ObjectNode quizStatistics(String quizId, User user) {
        var quiz = estadisticasService.obtenerEstadisticasQuiz(quizId, user);
        ObjectNode statistics = objectMapper.createObjectNode();
        statistics.put("entityName", quiz.nombreQuiz());
        statistics.set("metrics", objectMapper.valueToTree(quiz));
        try {
            statistics.set("summary", sanitizeQuizSummary(
                    objectMapper.valueToTree(
                            resumenVistasService.generalPorQuiz(quizId))));
        } catch (EntityNotFoundException exception) {
            statistics.putNull("summary");
        }
        statistics.set("students", sanitizeStudents(
                resumenVistasService.alumnos(quizId)));
        return statistics;
    }

    private ObjectNode groupStatistics(String grupoId, User user) {
        var grupo = estadisticasService.obtenerEstadisticasGrupo(grupoId, user);
        ObjectNode statistics = objectMapper.createObjectNode();
        statistics.put("entityName", grupo.nombreGrupo());
        statistics.set("metrics", objectMapper.valueToTree(grupo));
        statistics.set("trend", objectMapper.valueToTree(
                estadisticasService.obtenerTendenciaAprobacionGrupo(
                        grupoId, user)));
        return statistics;
    }

    private ObjectNode plantelStatistics(Long plantelId, User user) {
        var plantel = estadisticasService.obtenerEstadisticasPlantel(
                plantelId, user);
        ObjectNode statistics = objectMapper.createObjectNode();
        statistics.put("entityName", plantel.nombrePlantel());
        statistics.set("metrics", objectMapper.valueToTree(plantel));
        return statistics;
    }

    private ObjectNode institutionStatistics(User user) {
        ObjectNode statistics = objectMapper.createObjectNode();
        statistics.put("entityName", "Institución");
        statistics.set("metrics",
                objectMapper.valueToTree(
                        estadisticasService.obtenerEstadisticasGenerales(user)));
        statistics.set("subjects",
                objectMapper.valueToTree(
                        estadisticasService.obtenerEstadisticasMateria(user)));
        statistics.set("teachers",
                sanitizeTeachers(estadisticasService
                        .obtenerEstadisticasDocentes(user)));
        statistics.set("trend",
                objectMapper.valueToTree(
                        estadisticasService.obtenerTendenciaAprobacion(user)));
        statistics.set("groups",
                sanitizeGroups(estadisticasService
                        .obtenerResumenGrupos(user)));
        statistics.set("campuses",
                objectMapper.valueToTree(
                        estadisticasService.obtenerEstadisticasPlanteles(user)));
        return statistics;
    }

    private JsonNode sanitizeQuizSummary(JsonNode summary) {
        ObjectNode sanitized = objectMapper.createObjectNode();
        Set<String> allowed = Set.of(
                "quizId", "grupoClave", "grupoNombre",
                "numPreguntas", "numIntentos", "numRespondieron",
                "numEnCurso", "numAprobados", "numReprobados",
                "promedio", "calificacionMaxima", "calificacionMinima",
                "duracionPromedioSegundos", "plantelNombre");
        for (String name : summary.propertyNames()) {
            if (allowed.contains(name)) {
                sanitized.set(name, summary.get(name));
            }
        }
        return sanitized;
    }

    private JsonNode sanitizeStudents(
            List<AlumnoDesgloseDTO> students) {
        var sanitized = objectMapper.createArrayNode();
        int index = 1;
        for (var student : students) {
            ObjectNode item = objectMapper.createObjectNode();
            item.put("studentOrdinal", index++);
            item.put("finalizado", student.finalizado());
            item.put("correctas", student.correctas());
            item.put("total", student.total());
            item.put("calificacion", student.calificacion());
            var answers = objectMapper.createArrayNode();
            for (var answer : student.respuestas()) {
                ObjectNode sanitizedAnswer = objectMapper.createObjectNode();
                sanitizedAnswer.put("numero", answer.numero());
                sanitizedAnswer.put("pregunta", answer.pregunta());
                sanitizedAnswer.put(
                        "conceptoDescripcion",
                        answer.conceptoDescripcion());
                sanitizedAnswer.put(
                        "opcionElegida",
                        answer.opcionElegida());
                sanitizedAnswer.put(
                        "opcionCorrecta",
                        answer.opcionCorrecta());
                sanitizedAnswer.put("resultado", answer.resultado());
                sanitizedAnswer.put(
                        "cambiosPantalla",
                        answer.cambiosPantalla());
                answers.add(sanitizedAnswer);
            }
            item.set("respuestas", answers);
            sanitized.add(item);
        }
        return sanitized;
    }

    private JsonNode sanitizeTeachers(
            List<EstadisticasDocentesDTO> teachers) {
        return objectMapper.valueToTree(teachers.stream()
                .map(teacher -> Map.of(
                        "quizzesAplicados", teacher.quizzesAplicados(),
                        "intentosFinalizados", teacher.intentosFinalizados()))
                .toList());
    }

    private JsonNode sanitizeGroups(
            List<EstadisticasGrupoResumenDTO> groups) {
        var sanitized = new ArrayList<Map<String, Object>>();
        for (var group : groups) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("grupo", group.nombreGrupo());
            item.put("claveGrupo", group.claveGrupo());
            item.put("semestre", group.semestre());
            item.put("materia", group.nombreMateria());
            item.put("porcentajeAprobacion", group.porcentajeAprobacion());
            item.put("alumnosEvaluados", group.alumnosEvaluados());
            item.put("intentosFinalizados", group.intentosFinalizados());
            item.put("incidenciasFoco", group.incidenciasFoco());
            sanitized.add(item);
        }
        return objectMapper.valueToTree(sanitized);
    }

    private String requiredId(String entityId, String scope) {
        if (entityId == null || entityId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "entityId es obligatorio para el nivel " + scope + ".");
        }
        return entityId.trim();
    }

    private Long resolvePlantelId(
            String entityId,
            String scope,
            User user) {
        if (entityId == null || entityId.isBlank()) {
            if (user == null
                    || user.getPersona() == null
                    || user.getPersona().getPlantel() == null) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_CONTENT,
                        "entityId es obligatorio para el nivel " + scope + ".");
            }
            return user.getPersona().getPlantel().getId();
        }
        try {
            return Long.valueOf(entityId.trim());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "entityId no es válido para el nivel " + scope + ".");
        }
    }
}
