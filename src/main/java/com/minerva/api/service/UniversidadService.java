package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.UniversidadDTO;

public interface UniversidadService {
    UniversidadDTO saveUniversidad(UniversidadDTO universidadDTO);

    UniversidadDTO updateUniversidad(Long universidadId, UniversidadDTO universidadDTO);

    void deleteUniversidad(Long universidadId);

    void softDeleteUniversidad(Long universidadId);
    
    List<UniversidadDTO> findAll();

    UniversidadDTO getUniversidadById(Long universidadId);
}
