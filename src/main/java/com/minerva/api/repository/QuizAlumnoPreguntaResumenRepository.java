package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.QuizAlumnoPreguntaResumen;

public interface QuizAlumnoPreguntaResumenRepository extends JpaRepository <QuizAlumnoPreguntaResumen, String> {
    List<QuizAlumnoPreguntaResumen> findByQuizIdOrderByAlumnoNombreAscNumeroAsc(String quizId);

    List<QuizAlumnoPreguntaResumen> findByQuizIdAndAlumnoIdOrderByNumeroAsc(String quizId, String alumnoId);

    List<QuizAlumnoPreguntaResumen> findByGrupoId(String grupoId);
    
}
