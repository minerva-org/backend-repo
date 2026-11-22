package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.AlumnoDesgloseDTO;
import com.minerva.api.dto.ResumenesGeneralesDTO.ResumenGeneralDTO;
import com.minerva.api.dto.ResumenesGeneralesDTO.ResumenGeneralDocenteDTO;
import com.minerva.api.dto.ResumenesGeneralesDTO.ResumenGeneralGrupoDTO;
import com.minerva.api.dto.ResumenesGeneralesDTO.ResumenGeneralPlantelDTO;
import com.minerva.api.service.ResumenVistasService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quizzes/resumen")
@RequiredArgsConstructor
public class ResumenVistasController {

    private final ResumenVistasService resumenVistasService;

    @GetMapping
    public ResponseEntity<List<ResumenGeneralDTO>> resumenes(){
        return ResponseEntity.ok(resumenVistasService.general());
    }

    @GetMapping("/plantel/{plantelId}")
    public ResponseEntity<List<ResumenGeneralPlantelDTO>> resumenesPlantel(@RequestParam String plantelId){
        return ResponseEntity.ok(resumenVistasService.generalPorPlantel(plantelId));
    }
    
    @GetMapping("/docente/{docenteId}")
    public ResponseEntity<List<ResumenGeneralDocenteDTO>> resumenesDocente(@PathVariable String docenteId){
        return ResponseEntity.ok(resumenVistasService.generalPorDocente(docenteId));
    }
    
    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<ResumenGeneralGrupoDTO>> resumenGrupo(@PathVariable String grupoId){
        return ResponseEntity.ok(resumenVistasService.generalPorGrupo(grupoId));
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<ResumenGeneralDTO> generalQuiz(@PathVariable String quizId) {
        return ResponseEntity.ok(resumenVistasService.generalPorQuiz(quizId));
    }

    @GetMapping("/alumnos/quiz/{quizId}")
    public ResponseEntity<List<AlumnoDesgloseDTO>> alumnos(@PathVariable String quizId) {
        return ResponseEntity.ok(resumenVistasService.alumnos(quizId));
    }

    @GetMapping("/alumnos/grupo/{grupoId}")
    public ResponseEntity<List<AlumnoDesgloseDTO>> alumnosPorGrupo(@PathVariable String grupoId){
        return ResponseEntity.ok(resumenVistasService.alumnosPorGrupo(grupoId));
    }

    @GetMapping("/alumnos/{alumnoId}")
    public ResponseEntity<AlumnoDesgloseDTO> alumno(@PathVariable String quizId,
                                                    @PathVariable String alumnoId) {
        return ResponseEntity.ok(resumenVistasService.alumno(quizId, alumnoId));
    }
}