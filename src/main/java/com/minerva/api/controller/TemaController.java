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

import com.minerva.api.dto.TemaDTO;
import com.minerva.api.service.TemaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/temas")
@RequiredArgsConstructor
public class TemaController {

    private final TemaService temaService;

    @GetMapping
    public ResponseEntity<List<TemaDTO>> getAllTemas() {
        return ResponseEntity.ok(temaService.findAll());
    }

    @GetMapping(params = "unidadId")
    public ResponseEntity<List<TemaDTO>> getTemasByUnidad(@RequestParam String unidadId) {
        return ResponseEntity.ok(temaService.findAllByUnidadId(unidadId));
    }

    @GetMapping("/{temaId}")
    public ResponseEntity<TemaDTO> getTemaById(@PathVariable String temaId) {
        return ResponseEntity.ok(temaService.getTemaById(temaId));
    }

    @PostMapping
    public ResponseEntity<TemaDTO> saveTema(@RequestBody TemaDTO temaDTO) {
        TemaDTO temaCreado = temaService.saveTema(temaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(temaCreado);
    }

    @PatchMapping("/{temaId}")
    public ResponseEntity<TemaDTO> updateTema(
            @PathVariable String temaId, @RequestBody TemaDTO temaDTO) {
        return ResponseEntity.ok(temaService.updateTema(temaId, temaDTO));
    }

    @DeleteMapping("/{temaId}")
    public ResponseEntity<Void> deleteTema(@PathVariable String temaId) {
        temaService.deleteTema(temaId);
        return ResponseEntity.noContent().build();
    }
}