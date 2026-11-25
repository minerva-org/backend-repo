package com.minerva.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.User.User;
import com.minerva.api.dto.IntentoDTO;
import com.minerva.api.dto.RespuestaRequestDTO;
import com.minerva.api.dto.AlumnoDesgloseDTO;
import com.minerva.api.service.IntentoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quizzes/{quizId}/intentos")
@RequiredArgsConstructor
public class IntentoController {

        private final IntentoService intentoService;

        @PostMapping
        public ResponseEntity<IntentoDTO> iniciar(@PathVariable String quizId,
                        @AuthenticationPrincipal User user) {
                return ResponseEntity.ok(
                                intentoService.iniciarOReanudar(quizId, user.getPersona().getId()));
        }

        @PostMapping("/{intentoId}/respuestas")
        public ResponseEntity<IntentoDTO> responder(@PathVariable String quizId,
                        @PathVariable String intentoId,
                        @RequestBody RespuestaRequestDTO body,
                        @AuthenticationPrincipal User user) {
                return ResponseEntity.ok(
                                intentoService.responder(quizId, intentoId, user.getPersona().getId(), body));
        }

        @PostMapping("/{intentoId}/finalizar")
        public ResponseEntity<IntentoService.Resultado> finalizar(@PathVariable String quizId,
                        @PathVariable String intentoId,
                        @AuthenticationPrincipal User user) {
                return ResponseEntity.ok(
                                intentoService.finalizar(quizId, intentoId, user.getPersona().getId()));
        }

        @GetMapping("/{intentoId}/detalle-resultados")
        public ResponseEntity<AlumnoDesgloseDTO> detalleResultados(
                        @PathVariable String quizId,
                        @PathVariable String intentoId,
                        @AuthenticationPrincipal User user) {
                return ResponseEntity.ok(
                                intentoService.detalleResultados(
                                                quizId,
                                                intentoId,
                                                user.getPersona().getId()));
        }
}