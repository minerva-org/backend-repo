package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.QuizResumenGeneral;

public interface QuizResumenGeneralRepository extends JpaRepository<QuizResumenGeneral, String> {
    List<QuizResumenGeneral> findByDocenteId(String docenteId);
} 