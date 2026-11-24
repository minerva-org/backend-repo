package com.minerva.api.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.User.Roles;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Persona;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.PersonaRepository;
import com.minerva.api.security.RolAutorizacion;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
@RequiredArgsConstructor
public class QuizIaServiceImpl implements QuizIaService {
    private static final Logger LOGGER =
            Logger.getLogger(QuizIaServiceImpl.class.getName());

    private final GrupoRepository grupoRepository;
    private final PersonaRepository personaRepository;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;

    @Override
    public JsonNode generar(String grupoId, JsonNode request, String personaId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado."));
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada."));
        validarGestionGrupo(grupo, persona);

        if (request == null
                || !request.isObject()
                || request.get("prompt") == null
                || !request.get("prompt").isTextual()
                || request.get("prompt").asText().isBlank()
                || request.get("arbol") == null
                || !request.get("arbol").isObject()) {
            LOGGER.log(Level.WARNING,
                    "QUIZ_IA_SOLICITUD_INVALIDA: grupoId={0}, personaId={1}",
                    new Object[]{grupoId, personaId});
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "El prompt y el arbol curricular son obligatorios.");
        }

        String prompt = request.get("prompt").asText();
        LOGGER.log(Level.INFO,
                "QUIZ_IA_SOLICITUD: grupoId={0}, personaId={1}, promptChars={2}",
                new Object[]{grupoId, personaId, prompt.length()});

        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("prompt", prompt);
        datos.set("arbol", request.get("arbol"));

        ObjectNode taskRequest = objectMapper.createObjectNode();
        taskRequest.put("tipo", "QUIZ_GENERATE");
        taskRequest.set("datos", datos);
        JsonNode response = aiServiceClient.createTask(taskRequest);
        JsonNode tareaIdNode = response.get("tareaId");
        LOGGER.log(Level.INFO,
                "QUIZ_IA_TAREA_CREADA: grupoId={0}, tareaId={1}",
                new Object[]{grupoId,
                        tareaIdNode != null && tareaIdNode.isTextual()
                                ? tareaIdNode.asText()
                                : "desconocida"});
        return response;
    }

    @Override
    public JsonNode consultarTarea(
            String grupoId,
            String tareaId,
            String personaId) {
        validarAccesoGrupo(grupoId, personaId);
        return aiServiceClient.getTask(tareaId);
    }

    @Override
    public void cancelarTarea(
            String grupoId,
            String tareaId,
            String personaId) {
        validarAccesoGrupo(grupoId, personaId);
        LOGGER.log(Level.INFO,
                "QUIZ_IA_TAREA_CANCELADA: grupoId={0}, tareaId={1}",
                new Object[]{grupoId, tareaId});
        aiServiceClient.deleteTask(tareaId);
    }

    private void validarGestionGrupo(Grupo grupo, Persona persona) {
        if (persona == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para generar quizzes.");
        }
        if (grupo.getDocente() == null
                || !grupo.getDocente().getId().equals(persona.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el docente asignado puede generar quizzes.");
        }
    }

    private void validarAccesoGrupo(String grupoId, String personaId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado."));
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada."));
        Roles rol = persona.getRol();
        if (rol == Roles.ADMIN
                || rol == Roles.DEV
                || (grupo.getDocente() != null
                        && grupo.getDocente().getId().equals(persona.getId()))) {
            return;
        }
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No tienes permiso para consultar esta tarea.");
    }
}
