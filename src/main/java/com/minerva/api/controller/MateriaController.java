package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.minerva.api.dto.MateriaDTO;
import com.minerva.api.service.MateriaService;
import com.minerva.api.User.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.minerva.api.service.MateriaExtractionService;
import tools.jackson.databind.JsonNode;

@RestController 
@RequestMapping ("api/materias")
@RequiredArgsConstructor
public class MateriaController {

    private final MateriaService materiaService;
    private final MateriaExtractionService materiaExtractionService;

    @GetMapping 
    public ResponseEntity<List<MateriaDTO>> getMaterias(
            @RequestParam(required = false) Long plantelId,
            @AuthenticationPrincipal User usuario){
        return ResponseEntity.ok(materiaService.findMateriasVisibles(
                plantelId,
                usuario));
    }

    @GetMapping("/{materiaId}")
    public ResponseEntity<MateriaDTO> getMateriaById(@PathVariable String materiaId){
        return ResponseEntity.ok(materiaService.getMateriaById(materiaId));
    }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonNode> extractMateria(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User usuario) {
        return materiaExtractionService.extract(file, usuario);
    }

    @PostMapping
    public ResponseEntity<MateriaDTO> saveMateria(
            @RequestBody MateriaDTO materiaDTO,
            @AuthenticationPrincipal User usuario){
        MateriaDTO materiaCreada = materiaService.saveMateria(materiaDTO, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(materiaCreada);
    }

    @PatchMapping("/{materiaId}")
    public ResponseEntity<MateriaDTO> updateMateria(
            @PathVariable String materiaId,
            @RequestBody MateriaDTO materiaDTO,
            @RequestParam(required = false) Long plantelId,
            @AuthenticationPrincipal User usuario){
        return ResponseEntity.ok(materiaService.updateMateria(materiaId, materiaDTO, plantelId, usuario));
    }

    @PatchMapping("/{materiaId}/estado")
    public ResponseEntity<Void> updateEstadoMateria(
            @PathVariable String materiaId,
            @RequestParam Boolean estado,
            @AuthenticationPrincipal User usuario) {
        materiaService.updateEstado(materiaId, estado, usuario);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping ("/{materiaId}")
    public ResponseEntity<Void> deleteMateria(
            @PathVariable String materiaId,
            @AuthenticationPrincipal User usuario){
        materiaService.deleteMateria(materiaId, usuario);
        return ResponseEntity.noContent().build();
    }
}
