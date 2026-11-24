package com.minerva.api.service;

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
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "El prompt y el arbol curricular son obligatorios.");
        }

        ObjectNode datos = objectMapper.createObjectNode();
        datos.put("prompt", request.get("prompt").asText());
        datos.set("arbol", request.get("arbol"));

        ObjectNode taskRequest = objectMapper.createObjectNode();
        taskRequest.put("tipo", "QUIZ_GENERATE");
        taskRequest.set("datos", datos);
        return aiServiceClient.createTask(taskRequest);
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
