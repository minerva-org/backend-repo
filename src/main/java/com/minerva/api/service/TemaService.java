package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.TemaDTO;
import com.minerva.api.dto.UnidadDTO;

public interface TemaService {
    TemaDTO saveTema(TemaDTO temaDTO);

    TemaDTO updateTema(String temaId, TemaDTO temaDTO);

    void deleteTema(String temaId);
    
    List<TemaDTO> findAll();

    List<TemaDTO> findAllByUnidadId(String unidadId);

    TemaDTO getTemaById(String temaId);
}
