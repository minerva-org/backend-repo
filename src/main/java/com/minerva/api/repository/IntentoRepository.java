package com.minerva.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minerva.api.model.Intento;

public interface IntentoRepository extends JpaRepository<Intento, String> {
    boolean existsByQuizIdAndAlumnoId(String quizId, String alumnoId);

    Optional<Intento> findByQuizIdAndAlumnoId(String quizId, String alumnoId);

    List<Intento> findByQuizId(String quizId);

    @Query("""
        select i.id as intentoId,
                (select count(d) from IntentoDetalle d
                where d.intento = i and d.esCorrecto = true) as correctas
        from Intento i
        where i.quiz.id = :quizId and i.fechaEnvio is not null
        """)
    List<PuntajeIntento> puntajesPorQuiz(@Param("quizId") String quizId);
}
