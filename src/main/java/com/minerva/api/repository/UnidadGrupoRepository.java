package com.minerva.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.UnidadGrupo;

public interface UnidadGrupoRepository extends JpaRepository<UnidadGrupo, String> {

    Optional<UnidadGrupo> findByIdAndGrupoId(String id, String grupoId);

    List<UnidadGrupo> findByGrupoIdAndActivoTrueOrderByIdAsc(String grupoId);

    boolean existsByNombreIgnoreCaseAndGrupoId(String nombre, String grupoId);

    boolean existsByNombreIgnoreCaseAndGrupoIdAndIdNot(String nombre, String grupoId, String id);
}
