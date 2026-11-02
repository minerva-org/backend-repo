package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.ConfiguracionDTO;

public interface ConfiguracionService {
    ConfiguracionDTO saveConfiguracion(ConfiguracionDTO configuracionDTO);

    ConfiguracionDTO updateConfiguracion(String configuracionId, ConfiguracionDTO configuracionDTO);

    void deleteConfiguracion(String configuracionId);
    
    List<ConfiguracionDTO> findAll();

    ConfiguracionDTO getConfiguracionById(String ConfiguracionId);
}
