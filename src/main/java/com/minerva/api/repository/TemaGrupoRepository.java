package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.TemaGrupo;

public interface TemaGrupoRepository extends JpaRepository<TemaGrupo, String> {

    List<TemaGrupo> findByUnidadGrupoIdAndActivoTrueOrderByIdAsc(String unidadGrupoId);

    boolean existsByNombreIgnoreCaseAndUnidadGrupoId(String nombre, String unidadGrupoId);

    boolean existsByNombreIgnoreCaseAndUnidadGrupoIdAndIdNot(String nombre, String unidadGrupoId, String id);
}
