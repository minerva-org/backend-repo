package com.minerva.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.PreguntaDTO;
import com.minerva.api.dto.QuizDTO;
import com.minerva.api.service.QuizService;
import com.minerva.api.service.QuizXPreguntaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizXPreguntaService quizXPreguntaService;

    @GetMapping
    public ResponseEntity<List<QuizDTO>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.findAll());
    }

    @GetMapping(params = "grupoId")
    public ResponseEntity<List<QuizDTO>> getQuizzesByGrupo(@RequestParam String grupoId) {
        return ResponseEntity.ok(quizService.findAllByGrupoId(grupoId));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable String quizId) {
        return ResponseEntity.ok(quizService.getQuizById(quizId));
    }

    @GetMapping("/{quizId}/preguntas")
        public ResponseEntity<List<PreguntaDTO>> getPreguntasDelQuiz(@PathVariable String quizId) {
        return ResponseEntity.ok(quizXPreguntaService.findPreguntasByQuizId(quizId));
    }

    @PostMapping
    public ResponseEntity<QuizDTO> saveQuiz(@RequestBody QuizDTO quizDTO) {
        QuizDTO quizCreado = quizService.saveQuiz(quizDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(quizCreado);
    }

    @PatchMapping("/{quizId}")
    public ResponseEntity<QuizDTO> updateQuiz(
            @PathVariable String quizId, @RequestBody QuizDTO quizDTO) {
        return ResponseEntity.ok(quizService.updateQuiz(quizId, quizDTO));
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable String quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }
}