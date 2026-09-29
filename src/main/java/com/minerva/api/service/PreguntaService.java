package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.PreguntaDTO;

public interface PreguntaService {
    List<PreguntaDTO> findAll();

    List<PreguntaDTO> findAllByConceptoId(String conceptoId);

    PreguntaDTO getPreguntaById(String preguntaId);

    PreguntaDTO savePregunta(PreguntaDTO preguntaDTO);

    PreguntaDTO updatePregunta(String preguntaId, PreguntaDTO preguntaDTO);

    void deletePregunta(String preguntaId);
}