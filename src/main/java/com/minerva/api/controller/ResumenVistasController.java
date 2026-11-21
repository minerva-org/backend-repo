package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.AlumnoDesgloseDTO;
import com.minerva.api.dto.ResumenGeneralDTO;
import com.minerva.api.service.ResumenVistasService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quizzes/{quizId}/resumen")
@RequiredArgsConstructor
public class ResumenVistasController {

    private final ResumenVistasService resumenVistasService;

    @GetMapping("/general")
    public ResponseEntity<ResumenGeneralDTO> general(@PathVariable String quizId) {
        return ResponseEntity.ok(resumenVistasService.general(quizId));
    }

    @GetMapping("/docente/{docenteId}")
    public ResponseEntity<List<ResumenGeneralDTO>> resumenesDocente(@PathVariable String docenteId){
        return ResponseEntity.ok(resumenVistasService.generalPorDocente(docenteId));
    }

    @GetMapping("/alumnos")
    public ResponseEntity<List<AlumnoDesgloseDTO>> alumnos(@PathVariable String quizId) {
        return ResponseEntity.ok(resumenVistasService.alumnos(quizId));
    }

    @GetMapping("/alumnos/{alumnoId}")
    public ResponseEntity<AlumnoDesgloseDTO> alumno(@PathVariable String quizId,
                                                    @PathVariable String alumnoId) {
        return ResponseEntity.ok(resumenVistasService.alumno(quizId, alumnoId));
    }
}