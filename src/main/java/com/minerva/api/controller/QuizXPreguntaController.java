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
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.QuizXPreguntaDTO;
import com.minerva.api.service.QuizXPreguntaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quiz-x-pregunta")
@RequiredArgsConstructor
public class QuizXPreguntaController {

    private final QuizXPreguntaService quizXPreguntaService;

    @GetMapping
    public ResponseEntity<List<QuizXPreguntaDTO>> getAll() {
        return ResponseEntity.ok(quizXPreguntaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizXPreguntaDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(quizXPreguntaService.getQuizXPreguntaById(id));
    }

    @PostMapping
    public ResponseEntity<QuizXPreguntaDTO> save(@RequestBody QuizXPreguntaDTO dto) {
        QuizXPreguntaDTO creado = quizXPreguntaService.saveQuizXPregunta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<QuizXPreguntaDTO> update(
            @PathVariable String id, @RequestBody QuizXPreguntaDTO dto) {
        return ResponseEntity.ok(quizXPreguntaService.updateQuizXPregunta(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        quizXPreguntaService.deleteQuizXPregunta(id);
        return ResponseEntity.noContent().build();
    }
}