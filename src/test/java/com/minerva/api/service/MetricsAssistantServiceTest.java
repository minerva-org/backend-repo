package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.minerva.api.User.User;
import com.minerva.api.User.Roles;
import com.minerva.api.dto.MetricsAssistantRequestDTO;
import com.minerva.api.dto.AlumnoDesgloseDTO;
import com.minerva.api.dto.RespuestaAlumnoDTO;
import com.minerva.api.dto.EstadisticasDTO.EstadisticasGrupoDTO;
import com.minerva.api.dto.EstadisticasDTO.EstadisticasTendenciaDTO;
import com.minerva.api.dto.EstadisticasDTO.EstadisticasQuizDTO;
import com.minerva.api.model.Persona;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MetricsAssistantServiceTest {

    @Mock
    private EstadisticasService estadisticasService;
    @Mock
    private ResumenVistasService resumenVistasService;
    @Mock
    private AiServiceClient aiServiceClient;

    private MetricsAssistantService service;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new MetricsAssistantService(
                estadisticasService,
                resumenVistasService,
                aiServiceClient,
                mapper);
    }

    @Test
    void buildsGroupContextFromAuthorizedBackendServices() {
        User user = new User();
        Persona persona = new Persona();
        persona.setRol(Roles.DOCENTE);
        user.setPersona(persona);
        var group = new EstadisticasGrupoDTO(
                "GRP-1", "Grupo 1", "A1", "1",
                80, 75, 25, 90,
                20, 4, 30, 2, 22, 8);
        when(estadisticasService.obtenerEstadisticasGrupo("GRP-1", user))
                .thenReturn(group);
        when(estadisticasService.obtenerTendenciaAprobacionGrupo("GRP-1", user))
                .thenReturn(List.of(new EstadisticasTendenciaDTO(
                        "Q-1", "Quiz 1", null, 75, 20)));
        when(aiServiceClient.consultMetricsAssistant(any()))
                .thenReturn(mapper.readTree(
                        "{\"answer\":\"Análisis del grupo\",\"model\":\"test\"}"));

        var response = service.consult(
                new MetricsAssistantRequestDTO(
                        "GRUPO",
                        "GRP-1",
                        "Interpreta el desempeño",
                        List.of()),
                user);

        ArgumentCaptor<JsonNode> request =
                ArgumentCaptor.forClass(JsonNode.class);
        verify(aiServiceClient).consultMetricsAssistant(request.capture());
        JsonNode sent = request.getValue();
        assertEquals("GRUPO", sent.path("scope").asText());
        assertEquals("Grupo 1", sent.path("entityName").asText());
        assertEquals(80.0,
                sent.path("statistics").path("metrics")
                        .path("promedioGeneral").asDouble());
        assertTrue(sent.path("statistics").path("trend").isArray());
        assertEquals("Interpreta el desempeño",
                sent.path("messages").get(0).path("content").asText());
        assertEquals("Análisis del grupo", response.answer());
        verify(estadisticasService)
                .obtenerEstadisticasGrupo("GRP-1", user);
    }

    @Test
    void rejectsInstitutionScopeForTeacher() {
        User user = new User();
        Persona persona = new Persona();
        persona.setRol(Roles.DOCENTE);
        user.setPersona(persona);

        assertThrows(AccessDeniedException.class, () -> service.consult(
                new MetricsAssistantRequestDTO(
                        "INSTITUCION",
                        null,
                        "Muéstrame toda la institución",
                        List.of()),
                user));
    }

    @Test
    void removesStudentNamesAndIdentifiersFromQuizContext() {
        User user = new User();
        Persona persona = new Persona();
        persona.setRol(Roles.DOCENTE);
        user.setPersona(persona);
        when(estadisticasService.obtenerEstadisticasQuiz("QUIZ-1", user))
                .thenReturn(new EstadisticasQuizDTO(
                        "QUIZ-1", "Evaluación 1",
                        80, 75, 25, 90,
                        1, 1, 0, 1, 0));
        when(resumenVistasService.alumnos("QUIZ-1"))
                .thenReturn(List.of(new AlumnoDesgloseDTO(
                        "AL-SECRET", "Nombre Confidencial",
                        "GRP-1", "A1", "Grupo 1",
                        "INTENTO-SECRET", true,
                        1, 1, 100,
                        List.of(new RespuestaAlumnoDTO(
                                1, "P-1", "Pregunta",
                                "C-1", "Concepto",
                                "Respuesta", "Respuesta",
                                "CORRECTA", 2)))));
        when(aiServiceClient.consultMetricsAssistant(any()))
                .thenReturn(mapper.readTree(
                        "{\"answer\":\"Análisis\",\"model\":\"test\"}"));

        service.consult(new MetricsAssistantRequestDTO(
                "QUIZ", "QUIZ-1", "Analiza", List.of()), user);

        ArgumentCaptor<JsonNode> request =
                ArgumentCaptor.forClass(JsonNode.class);
        verify(aiServiceClient).consultMetricsAssistant(request.capture());
        String payload = request.getValue().toString();
        assertTrue(payload.contains("\"studentOrdinal\":1"));
        assertTrue(!payload.contains("Nombre Confidencial"));
        assertTrue(!payload.contains("AL-SECRET"));
        assertTrue(!payload.contains("INTENTO-SECRET"));
    }
}
