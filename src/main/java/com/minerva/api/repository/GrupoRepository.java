package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.Grupo;

public interface GrupoRepository extends JpaRepository<Grupo, String> {

    boolean existsByClaveGrupoIgnoreCase(String claveGrupo);

    boolean existsByClaveGrupoIgnoreCaseAndIdNot(String claveGrupo, String id);

    List<Grupo> findByDocenteId(String docenteId);

    List<Grupo> findByPlantelId(Long plantelId);

    List<Grupo> findDistinctByAlumnosId(String alumnoId);
}