package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.PeriodoDTO;
import com.minerva.api.service.PeriodoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/periodo")
public class PeriodoController {
    private final PeriodoService periodoService;

    @GetMapping
    public ResponseEntity<List<PeriodoDTO>> getAllPeriodos(){
        return ResponseEntity.ok(periodoService.findAll());
    }
    
    @PostMapping
    public ResponseEntity<PeriodoDTO> savePeriodo(){
        PeriodoDTO nvoPeriodo = periodoService.nuevoPeriodo();
        return ResponseEntity.status(HttpStatus.CREATED).body(nvoPeriodo);
    }
}
