package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.User.User;
import com.minerva.api.dto.PreguntaDTO;
import com.minerva.api.dto.QuizDTO;
import com.minerva.api.dto.IntentoEstadoDTO;
import com.minerva.api.service.IntentoService;
import com.minerva.api.service.QuizService;
import com.minerva.api.service.QuizXPreguntaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizXPreguntaService quizXPreguntaService;
    private final IntentoService intentoService;

    @GetMapping
    public ResponseEntity<List<QuizDTO>> getAllQuizzes(
            @AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(quizService.findAll(usuario));
    }

    @GetMapping(params = "grupoId")
    public ResponseEntity<List<QuizDTO>> getQuizzesByGrupo(
            @RequestParam String grupoId,
            @AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(quizService.findAllByGrupoId(grupoId, usuario));
    }

    @GetMapping("/mis-quizzes")
    public ResponseEntity<List<QuizDTO>> getMisQuizzes(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(quizService.findAllByAlumnoId(user.getPersona().getId()));
    }

    @GetMapping("/mis-intentos")
    public ResponseEntity<List<IntentoEstadoDTO>> getMisIntentos(
            @AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(intentoService.misIntentos(usuario.getPersona().getId()));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDTO> getQuizById(
            @PathVariable String quizId,
            @AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(quizService.getQuizById(quizId, usuario));
    }

    @GetMapping("/{quizId}/preguntas")
    public ResponseEntity<List<PreguntaDTO>> getPreguntasDelQuiz(
            @PathVariable String quizId,
            @AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(quizXPreguntaService.findPreguntasByQuizId(quizId, usuario));
    }

    @PostMapping
    public ResponseEntity<QuizDTO> saveQuiz(
            @RequestBody QuizDTO quizDTO,
            @AuthenticationPrincipal User usuario) {
        QuizDTO quizCreado = quizService.saveQuiz(quizDTO, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(quizCreado);
    }

    @PatchMapping("/{quizId}")
    public ResponseEntity<QuizDTO> updateQuiz(
            @PathVariable String quizId,
            @RequestBody QuizDTO quizDTO,
            @AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(quizService.updateQuiz(quizId, quizDTO, usuario));
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable String quizId,
            @AuthenticationPrincipal User usuario) {
        quizService.deleteQuiz(quizId, usuario);
        return ResponseEntity.noContent().build();
    }
}