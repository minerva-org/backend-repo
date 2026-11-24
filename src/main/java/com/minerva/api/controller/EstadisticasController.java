package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.User.User;
import com.minerva.api.dto.EstadisticasDTO.*;
import com.minerva.api.service.EstadisticasService;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

        private final EstadisticasService estadisticasService;

        public EstadisticasController(
                        EstadisticasService estadisticasService) {
                this.estadisticasService = estadisticasService;
        }

        @GetMapping("/planteles")
        public ResponseEntity<List<EstadisticasPlantelDTO>> obtenerEstadisticasPlanteles(
                        @AuthenticationPrincipal User usuario) {
                return ResponseEntity.ok(estadisticasService.obtenerEstadisticasPlanteles(usuario));
        }

        @GetMapping("/quizzes")
        public ResponseEntity<EstadisticasGeneralesDTO> obtenerEstadisticas(
                        @AuthenticationPrincipal User usuario) {

                return ResponseEntity.ok(
                                estadisticasService.obtenerEstadisticasGenerales(usuario));
        }

        @GetMapping("/quizzes/{quizId}")
        public ResponseEntity<EstadisticasQuizDTO> obtenerEstadisticasQuiz(
                        @PathVariable String quizId, @AuthenticationPrincipal User usuario) {
                return ResponseEntity.ok(estadisticasService.obtenerEstadisticasQuiz(quizId, usuario));
        }

        @GetMapping("/grupos")
        public ResponseEntity<List<EstadisticasGrupoResumenDTO>> obtenerResumenGrupos(
                        @AuthenticationPrincipal User usuario) {
                return ResponseEntity.ok(estadisticasService.obtenerResumenGrupos(usuario));
        }

        @GetMapping("/grupos/{grupoId}")
        public ResponseEntity<EstadisticasGrupoDTO> obtenerEstadisticasGrupo(
                        @PathVariable String grupoId, @AuthenticationPrincipal User usuario) {
                return ResponseEntity.ok(estadisticasService.obtenerEstadisticasGrupo(grupoId, usuario));
        }

        @GetMapping("/materias")
        public ResponseEntity<List<EstadisticasMateriaDTO>> obtenerEstadisticasMateria(
                        @AuthenticationPrincipal User usuario) {
                return ResponseEntity.ok(estadisticasService.obtenerEstadisticasMateria(usuario));
        }

        @GetMapping("/docentes")
        public ResponseEntity<List<EstadisticasDocentesDTO>> obtenerEstadisticasDocentes(
                        @AuthenticationPrincipal User usuario) {
                return ResponseEntity.ok(estadisticasService.obtenerEstadisticasDocentes(usuario));
        }

        @GetMapping("/tendencia")
        public ResponseEntity<List<EstadisticasTendenciaDTO>> obtenerTendenciaAprobacion(
                        @AuthenticationPrincipal User usuario) {
                return ResponseEntity.ok(estadisticasService.obtenerTendenciaAprobacion(usuario));
        }

}