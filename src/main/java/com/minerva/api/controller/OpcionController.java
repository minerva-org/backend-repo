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

import com.minerva.api.dto.OpcionDTO;
import com.minerva.api.service.OpcionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/opciones")
@RequiredArgsConstructor
public class OpcionController {

    private final OpcionService opcionService;

    @GetMapping
    public ResponseEntity<List<OpcionDTO>> getAllOpciones() {
        return ResponseEntity.ok(opcionService.findAll());
    }

    @GetMapping(params = "preguntaId")
    public ResponseEntity<List<OpcionDTO>> getOpcionesByPregunta(@RequestParam String preguntaId) {
        return ResponseEntity.ok(opcionService.findAllByPreguntaId(preguntaId));
    }

    @GetMapping("/{opcionId}")
    public ResponseEntity<OpcionDTO> getOpcionById(@PathVariable String opcionId) {
        return ResponseEntity.ok(opcionService.getOpcionById(opcionId));
    }

    @PostMapping
    public ResponseEntity<OpcionDTO> saveOpcion(@RequestBody OpcionDTO opcionDTO) {
        OpcionDTO opcionCreada = opcionService.saveOpcion(opcionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(opcionCreada);
    }

    @PatchMapping("/{opcionId}")
    public ResponseEntity<OpcionDTO> updateOpcion(
            @PathVariable String opcionId, @RequestBody OpcionDTO opcionDTO) {
        return ResponseEntity.ok(opcionService.updateOpcion(opcionId, opcionDTO));
    }

    @DeleteMapping("/{opcionId}")
    public ResponseEntity<Void> deleteOpcion(@PathVariable String opcionId) {
        opcionService.deleteOpcion(opcionId);
        return ResponseEntity.noContent().build();
    }
}