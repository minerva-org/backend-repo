package com.minerva.api.service;

import java.util.List;
import com.minerva.api.dto.UnidadDTO;

public interface UnidadService {
    UnidadDTO saveUnidad(UnidadDTO unidadDTO);

    UnidadDTO updateUnidad(String unidadId, UnidadDTO unidadDTO);

    void deleteUnidad(String unidadId);
    
    List<UnidadDTO> findAll();

    List<UnidadDTO> findAllByMateriaId(String materiaId);

    UnidadDTO getUnidadById(String unidadId);
}
