package com.minerva.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.minerva.api.dto.CurriculoGrupoDTO;
import com.minerva.api.dto.UnidadGrupoDTO;
import com.minerva.api.service.CurriculoGrupoService;
import com.minerva.api.service.CurriculoIaService;
import com.minerva.api.User.User;
import tools.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
public class CurriculoGrupoController {

    private final CurriculoGrupoService curriculoGrupoService;
    private final CurriculoIaService curriculoIaService;

    @GetMapping("/{grupoId}/curriculo")
    public ResponseEntity<CurriculoGrupoDTO> getCurriculo(@PathVariable String grupoId) {
        return ResponseEntity.ok(curriculoGrupoService.findByGrupoId(grupoId));
    }

    @PutMapping("/{grupoId}/curriculo")
    public ResponseEntity<CurriculoGrupoDTO> replaceCurriculo(
            @PathVariable String grupoId,
            @RequestBody CurriculoGrupoDTO curriculo) {
        return ResponseEntity.ok(curriculoGrupoService.replaceCurriculo(grupoId, curriculo));
    }

    @GetMapping("/{grupoId}/curriculo/unidades/{unidadGrupoId}")
    public ResponseEntity<UnidadGrupoDTO> getUnidad(
            @PathVariable String grupoId,
            @PathVariable String unidadGrupoId) {
        return ResponseEntity.ok(curriculoGrupoService.findUnidadById(grupoId, unidadGrupoId));
    }

    @PostMapping("/{grupoId}/curriculo/generar")
    public ResponseEntity<JsonNode> generarCurriculo(
            @PathVariable String grupoId,
            @RequestBody JsonNode request,
            @AuthenticationPrincipal User usuario) {
        JsonNode response = curriculoIaService.generar(
                grupoId,
                request,
                usuario.getPersona().getId());
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/{grupoId}/curriculo/tareas/{tareaId}")
    public ResponseEntity<JsonNode> consultarGeneracion(
            @PathVariable String grupoId,
            @PathVariable String tareaId,
            @AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(
                curriculoIaService.consultarTarea(
                        grupoId,
                        tareaId,
                        usuario.getPersona().getId()));
    }

    @DeleteMapping("/{grupoId}/curriculo/tareas/{tareaId}")
    public ResponseEntity<Void> cancelarGeneracion(
            @PathVariable String grupoId,
            @PathVariable String tareaId,
            @AuthenticationPrincipal User usuario) {
        curriculoIaService.cancelarTarea(
                grupoId,
                tareaId,
                usuario.getPersona().getId());
        return ResponseEntity.noContent().build();
    }
}
