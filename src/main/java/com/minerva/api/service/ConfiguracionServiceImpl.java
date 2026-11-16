package com.minerva.api.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.minerva.api.dto.ConfiguracionDTO;
import com.minerva.api.dto.PeriodoDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Configuracion;
import com.minerva.api.model.Periodo;
import com.minerva.api.repository.ConfiguracionRepository;
import com.minerva.api.repository.PeriodoRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfiguracionServiceImpl implements ConfiguracionService{

    private final ConfiguracionRepository configuracionRepository;
    private final PeriodoRepository periodoRepository;

    @Override
    public ConfiguracionDTO saveConfiguracion(Configuracion configuracion) {
        Configuracion config = configuracionRepository.findById(configuracion.getId())
            .orElseThrow(() -> new EntityNotFoundException("No se encontró la configuracion con la siguiente ID: " + configuracion.getId()));

        config.setDescripcion(configuracion.getDescripcion());
        config.setValor(configuracion.getValor());
        config.setFechaActualizacion(configuracion.getFechaActualizacion());

        return Mapper.toDTO(config);
    }

    @Override
    public List<ConfiguracionDTO> findAll() {
        return configuracionRepository.findAll()
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    public ConfiguracionDTO findConfiguracionById(String configId){
        Configuracion config = configuracionRepository.findById(configId)
            .orElseThrow(() -> new EntityNotFoundException("No se encontró la configuración con el siguiente ID: " + configId));

        return Mapper.toDTO(config);
    }

    @Override
    public ConfiguracionDTO cambiarPeriodoActual(String periodoId){
        if(periodoId == null)
            throw new IllegalArgumentException("El periodo es obligatorio para el cambio");

        Periodo periodoGuardar = periodoRepository.findById(periodoId)
            .orElseThrow(() -> new EntityNotFoundException("No se encontró un periodo con el siguiente ID: " + periodoId));

        Configuracion configPeriodo = configuracionRepository.findById("PERIODO")
            .orElseThrow(()-> new EntityNotFoundException("Esa configuracion no ha sido agregada"));

        String descripcion = periodoGuardar.getDescripcion() + " " + periodoGuardar.getCurso();

        configPeriodo.setDescripcion(descripcion);
        configPeriodo.setValor(periodoGuardar.getId());
        configPeriodo.setFechaActualizacion(Instant.now());
        configuracionRepository.save(configPeriodo);

        return Mapper.toDTO(configPeriodo);
    }

    @Override
    public ConfiguracionDTO getConfiguracionById(String configuracionId) {
        if(configuracionId == null || configuracionId.isBlank()){
            throw new IllegalArgumentException("El id es obligatorio");
        }

        Configuracion config = configuracionRepository.findById(configuracionId)
            .orElseThrow(() -> new EntityNotFoundException("No se encontró la configuración con el siguiente ID: " + configuracionId));

        return Mapper.toDTO(config);
    }
    
}
