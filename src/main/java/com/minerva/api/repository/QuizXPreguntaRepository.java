package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.QuizXPregunta;

public interface QuizXPreguntaRepository extends JpaRepository<QuizXPregunta, String> {

    List<QuizXPregunta> findByQuizId(String quizId);

    List<QuizXPregunta> findByPreguntaId(String preguntaId);

    boolean existsByQuizIdAndPreguntaId(String quizId, String preguntaId);

    boolean existsByQuizIdAndPreguntaIdAndIdNot(String quizId, String preguntaId, String id);
}