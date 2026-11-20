package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.ConceptoGrupo;

public interface ConceptoGrupoRepository extends JpaRepository<ConceptoGrupo, String> {

    List<ConceptoGrupo> findByTemaGrupoIdAndActivoTrueOrderByIdAsc(String temaGrupoId);

    boolean existsByNombreIgnoreCaseAndTemaGrupoId(String nombre, String temaGrupoId);

    boolean existsByNombreIgnoreCaseAndTemaGrupoIdAndIdNot(String nombre, String temaGrupoId, String id);
}
