package com.minerva.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minerva.api.model.ResumenQuiz;

import jakarta.persistence.LockModeType;

public interface ResumenQuizRepository extends JpaRepository<ResumenQuiz, String> {

    Optional<ResumenQuiz> findByQuizId(String quizId);

    // Bloquea la fila
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ResumenQuiz r where r.quiz.id = :quizId")
    Optional<ResumenQuiz> findByQuizIdForUpdate(@Param("quizId") String quizId);
}