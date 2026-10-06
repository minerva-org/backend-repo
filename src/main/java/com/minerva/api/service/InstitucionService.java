package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.InstitucionDTO;

public interface InstitucionService {
    InstitucionDTO saveInstitucion(InstitucionDTO institucionDTO);

    InstitucionDTO updateInstitucion(Long institucionId, InstitucionDTO institucionDTO);

    void deleteInstitucion(Long institucionId);

    void softDeleteInstitucion(Long institucionId);
    
    List<InstitucionDTO> findAll();

    InstitucionDTO getInstitucionById(Long institucionId);
}
