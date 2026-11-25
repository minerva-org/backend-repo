package com.minerva.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
        if (answer == null || !answer.isTextual() || answer.asText().isBlank()) {
            throw new AiServiceClient.AiServiceUnavailableException(
                    "AI-Service returned an invalid metrics assistant response.");
        }
        return new MetricsAssistantResponseDTO(
                answer.asText(),
                model != null && model.isTextual() ? model.asText() : "unknown");
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
            statistics.set("summary",
                    objectMapper.valueToTree(
                            resumenVistasService.generalPorQuiz(quizId)));
        } catch (EntityNotFoundException exception) {
            statistics.putNull("summary");
        }
        statistics.set("students",
                objectMapper.valueToTree(resumenVistasService.alumnos(quizId)));
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
                objectMapper.valueToTree(
                        estadisticasService.obtenerEstadisticasDocentes(user)));
        statistics.set("trend",
                objectMapper.valueToTree(
                        estadisticasService.obtenerTendenciaAprobacion(user)));
        statistics.set("groups",
                objectMapper.valueToTree(
                        estadisticasService.obtenerResumenGrupos(user)));
        statistics.set("campuses",
                objectMapper.valueToTree(
                        estadisticasService.obtenerEstadisticasPlanteles(user)));
        return statistics;
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
