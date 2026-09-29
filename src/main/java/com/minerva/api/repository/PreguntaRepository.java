package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Pregunta;

public interface PreguntaRepository extends JpaRepository<Pregunta, String> {

    List<Pregunta> findByConceptoId(String conceptoId);
}