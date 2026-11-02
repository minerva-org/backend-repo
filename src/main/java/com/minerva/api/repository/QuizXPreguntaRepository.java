package com.minerva.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable; 
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.QuizXPregunta;

public interface QuizXPreguntaRepository extends JpaRepository<QuizXPregunta, String> {

    List<QuizXPregunta> findByQuizId(String quizId);

    List<QuizXPregunta> findByPreguntaId(String preguntaId);

    boolean existsByQuizIdAndPreguntaId(String quizId, String preguntaId);

    boolean existsByQuizIdAndPreguntaIdAndIdNot(String quizId, String preguntaId, String id);

        // Todas las preguntas, en orden (se usa al cerrar el intento)
    @EntityGraph(attributePaths = "pregunta")
    List<QuizXPregunta> findByQuizIdOrderByIdAsc(String quizId);

    // Una sola pregunta por posición: PageRequest.of(posicion, 1)
    @EntityGraph(attributePaths = "pregunta")
    List<QuizXPregunta> findByQuizIdOrderByIdAsc(String quizId, Pageable pageable);

    long countByQuizId(String quizId);

    Optional<QuizXPregunta> findTopByQuizIdOrderByIdDesc(String quizId);
    
}