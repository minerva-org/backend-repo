package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.FuentesGrupo;
import com.minerva.api.model.FuentesGrupoId;

public interface FuentesGrupoRepository extends JpaRepository<FuentesGrupo, FuentesGrupoId> {

    List<FuentesGrupo> findByGrupoIdOrderByDocumentoCreatedAtDesc(String grupoId);

    List<FuentesGrupo> findByDocumentoIdOrderByGrupoIdAsc(String documentoId);

    boolean existsByIdIdDocumentoAndIdIdGrupo(String documentoId, String grupoId);
}
