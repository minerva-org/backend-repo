package com.minerva.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Opcion;

public interface OpcionRepository extends JpaRepository<Opcion, String> {

    List<Opcion> findByPreguntaId(String preguntaId);

    boolean existsByPreguntaIdAndEsCorrectaTrue(String preguntaId);

    boolean existsByPreguntaIdAndEsCorrectaTrueAndIdNot(String preguntaId, String id);

    List<Opcion> findByPreguntaIdAndEsCorrectaTrueAndIdNot(String preguntaId, String id);

    Optional<Opcion> findByIdAndPreguntaId(String id, String preguntaId);

    List<Opcion> findByPreguntaIdOrderByIdAsc(String preguntaId);
}