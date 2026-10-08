package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.ConceptoDTO;

public interface ConceptoService {
    ConceptoDTO saveConcepto(ConceptoDTO conceptoDTO);

    ConceptoDTO updateConcepto(String conceptoId, ConceptoDTO conceptoDTO);

    void deleteConcepto(String conceptoId);

    void softDeleteConcepto(String conceptoId);
    
    List<ConceptoDTO> findAll();

    List<ConceptoDTO> findAllByTemaId(String temaId);

    ConceptoDTO getConceptoById(String conceptoId);
}
