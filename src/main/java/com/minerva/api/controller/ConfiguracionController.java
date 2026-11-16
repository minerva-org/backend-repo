package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.ConfiguracionDTO;
import com.minerva.api.service.ConfiguracionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping ("api/config")
public class ConfiguracionController{
    private final ConfiguracionService configuracionService;

    @PatchMapping("/cambiar-periodo/{periodoId}")
    public ResponseEntity<ConfiguracionDTO> cambiarPeriodoActual(@PathVariable String periodoId){
        ConfiguracionDTO config = configuracionService.cambiarPeriodoActual(periodoId);
        return ResponseEntity.ok(config);
    }

    @GetMapping
    public ResponseEntity<List<ConfiguracionDTO>> findAllConfiguraciones(){
        return ResponseEntity.ok(configuracionService.findAll());
    }

}