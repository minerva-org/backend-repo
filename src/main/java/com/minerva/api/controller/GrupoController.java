package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.minerva.api.User.User;
import com.minerva.api.dto.GrupoDTO;
import com.minerva.api.service.GrupoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    @GetMapping
    public ResponseEntity<List<GrupoDTO>> getAllGrupos() {
        return ResponseEntity.ok(grupoService.findAll());
    }

    @GetMapping(params = "docenteId")
    public ResponseEntity<List<GrupoDTO>> getGruposByDocente(@RequestParam String docenteId) {
        return ResponseEntity.ok(grupoService.findAllByDocenteId(docenteId));
    }

    @GetMapping(params = "plantelId")
    public ResponseEntity<List<GrupoDTO>> getGruposByPlantel(@RequestParam Long plantelId) {
        return ResponseEntity.ok(grupoService.findAllByPlantelId(plantelId));
    }

    @GetMapping("/mis-grupos")
    public ResponseEntity<List<GrupoDTO>> getMisGrupos(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(grupoService.findAllByAlumnoId(user.getPersona().getId()));
    }

    @GetMapping("/{grupoId}")
    public ResponseEntity<GrupoDTO> getGrupoById(@PathVariable String grupoId) {
        return ResponseEntity.ok(grupoService.getGrupoById(grupoId));
    }

    @PostMapping
    public ResponseEntity<GrupoDTO> saveGrupo(@RequestBody GrupoDTO grupoDTO) {
        GrupoDTO grupoCreado = grupoService.saveGrupo(grupoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoCreado);
    }

    @PatchMapping("/{grupoId}")
    public ResponseEntity<GrupoDTO> updateGrupo(
            @PathVariable String grupoId, @RequestBody GrupoDTO grupoDTO) {
        return ResponseEntity.ok(grupoService.updateGrupo(grupoId, grupoDTO));
    }

    @DeleteMapping("/{grupoId}")
    public ResponseEntity<Void> deleteGrupo(@PathVariable String grupoId) {
        grupoService.deleteGrupo(grupoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{grupoId}/alumnos")
    public ResponseEntity<List<String>> getAlumnosDeGrupo(@PathVariable String grupoId) {
        return ResponseEntity.ok(grupoService.getAlumnosIdsDeGrupo(grupoId));
    }

    @PostMapping("/{grupoId}/alumnos/{alumnoId}")
    public ResponseEntity<Void> agregarAlumno(
            @PathVariable String grupoId, @PathVariable String alumnoId) {
        grupoService.agregarAlumno(grupoId, alumnoId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{grupoId}/alumnos/{alumnoId}")
    public ResponseEntity<Void> quitarAlumno(
            @PathVariable String grupoId, @PathVariable String alumnoId) {
        grupoService.quitarAlumno(grupoId, alumnoId);
        return ResponseEntity.noContent().build();
    }
}