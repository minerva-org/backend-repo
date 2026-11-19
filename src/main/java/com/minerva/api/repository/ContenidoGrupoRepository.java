package com.minerva.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.ContenidoGrupo;

public interface ContenidoGrupoRepository extends JpaRepository<ContenidoGrupo, String> {

    List<ContenidoGrupo> findByConceptoGrupoIdOrderByIdAsc(String conceptoGrupoId);
}
