package com.minerva.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.User.User;
import com.minerva.api.dto.EstadisticasGeneralesDTO;
import com.minerva.api.dto.EstadisticasQuizDTO;
import com.minerva.api.dto.EstadisticasGrupoDTO;
import com.minerva.api.service.EstadisticasService;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

        private final EstadisticasService estadisticasService;

        public EstadisticasController(
                        EstadisticasService estadisticasService) {
                this.estadisticasService = estadisticasService;
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

        @GetMapping("/grupos/{grupoId}")
        public ResponseEntity<EstadisticasGrupoDTO> obtenerEstadisticasGrupo(
                        @PathVariable String grupoId, @AuthenticationPrincipal User usuario) {
                return ResponseEntity.ok(estadisticasService.obtenerEstadisticasGrupo(grupoId, usuario));
        }
}