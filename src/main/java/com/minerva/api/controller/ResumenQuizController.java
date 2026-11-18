package com.minerva.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.dto.ResumenQuizDTO;
import com.minerva.api.service.ResumenQuizService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quizzes/{quizId}/resumen")
@RequiredArgsConstructor
public class ResumenQuizController {

    private final ResumenQuizService resumenQuizService;

    @GetMapping
    public ResponseEntity<ResumenQuizDTO> obtener(@PathVariable String quizId) {
        return ResponseEntity.ok(resumenQuizService.obtener(quizId));
    }

    @PostMapping("/recalcular")
    public ResponseEntity<ResumenQuizDTO> recalcular(@PathVariable String quizId) {
        return ResponseEntity.ok(resumenQuizService.recalcular(quizId));
    }
}