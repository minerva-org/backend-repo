package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.OpcionDTO;

public interface OpcionService {
    List<OpcionDTO> findAll();

    List<OpcionDTO> findAllByPreguntaId(String preguntaId);

    OpcionDTO getOpcionById(String opcionId);

    OpcionDTO saveOpcion(OpcionDTO opcionDTO);

    OpcionDTO updateOpcion(String opcionId, OpcionDTO opcionDTO);
    
    void deleteOpcion(String opcionId);
}