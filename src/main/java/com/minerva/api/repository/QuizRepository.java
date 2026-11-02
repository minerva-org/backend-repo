package com.minerva.api.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, String> {

    List<Quiz> findByGrupoId(String grupoId);

    List<Quiz> findByGrupoIdIn(Collection<String> grupoIds);

    boolean existsByNombreIgnoreCaseAndGrupoId(String nombre, String grupoId);

    boolean existsByNombreIgnoreCaseAndGrupoIdAndIdNot(String nombre, String grupoId, String id);
}