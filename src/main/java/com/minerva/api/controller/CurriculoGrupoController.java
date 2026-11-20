package com.minerva.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.CurriculoGrupoDTO;
import com.minerva.api.dto.UnidadGrupoDTO;
import com.minerva.api.service.CurriculoGrupoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
public class CurriculoGrupoController {

    private final CurriculoGrupoService curriculoGrupoService;

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
        return ResponseEntity.ok(curriculoGrupoService.findUnidadById(unidadGrupoId));
    }
}
