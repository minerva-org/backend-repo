package com.minerva.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Intento;

public interface IntentoRepository extends JpaRepository<Intento, String> {
    boolean existsByQuizIdAndAlumnoId(String quizId, String alumnoId);

    Optional<Intento> findByQuizIdAndAlumnoId(String quizId, String alumnoId);

    List<Intento> findByQuizId(String quizId);
}
