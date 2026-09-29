package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.PreguntaDTO;
import com.minerva.api.service.PreguntaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/preguntas")
@RequiredArgsConstructor
public class PreguntaController {

    private final PreguntaService preguntaService;

    @GetMapping
    public ResponseEntity<List<PreguntaDTO>> getAllPreguntas() {
        return ResponseEntity.ok(preguntaService.findAll());
    }

    @GetMapping(params = "conceptoId")
    public ResponseEntity<List<PreguntaDTO>> getPreguntasByConcepto(@RequestParam String conceptoId) {
        return ResponseEntity.ok(preguntaService.findAllByConceptoId(conceptoId));
    }

    @GetMapping("/{preguntaId}")
    public ResponseEntity<PreguntaDTO> getPreguntaById(@PathVariable String preguntaId) {
        return ResponseEntity.ok(preguntaService.getPreguntaById(preguntaId));
    }

    @PostMapping
    public ResponseEntity<PreguntaDTO> savePregunta(@RequestBody PreguntaDTO preguntaDTO) {
        PreguntaDTO preguntaCreada = preguntaService.savePregunta(preguntaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(preguntaCreada);
    }

    @PatchMapping("/{preguntaId}")
    public ResponseEntity<PreguntaDTO> updatePregunta(
            @PathVariable String preguntaId, @RequestBody PreguntaDTO preguntaDTO) {
        return ResponseEntity.ok(preguntaService.updatePregunta(preguntaId, preguntaDTO));
    }

    @DeleteMapping("/{preguntaId}")
    public ResponseEntity<Void> deletePregunta(@PathVariable String preguntaId) {
        preguntaService.deletePregunta(preguntaId);
        return ResponseEntity.noContent().build();
    }
}