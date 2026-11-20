package com.minerva.api.service;

import java.util.List;


import com.minerva.api.dto.ConfiguracionDTO;
import com.minerva.api.model.Configuracion;

public interface ConfiguracionService {
    ConfiguracionDTO saveConfiguracion(Configuracion configuracion);
    
    List<ConfiguracionDTO> findAll();

    ConfiguracionDTO findConfiguracionById(String configId);

    ConfiguracionDTO getConfiguracionById(String ConfiguracionId);

    ConfiguracionDTO cambiarPeriodoActual(String periodoId);
}
