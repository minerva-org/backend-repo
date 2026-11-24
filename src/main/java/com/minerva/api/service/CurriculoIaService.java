package com.minerva.api.service;

import tools.jackson.databind.JsonNode;

public interface CurriculoIaService {
    JsonNode generar(String grupoId, JsonNode request, String personaId);

    JsonNode consultarTarea(String grupoId, String tareaId, String personaId);

    void cancelarTarea(String grupoId, String tareaId, String personaId);
}
