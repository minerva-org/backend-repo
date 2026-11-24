package com.minerva.api.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minerva.api.model.SolicitudesGrupos;

public interface SolicitudesGruposRepository extends JpaRepository <SolicitudesGrupos, String> {

    boolean existsByAlumnoIdAndGrupoId(String alumnoId, String grupoId);

    List<SolicitudesGrupos> findByGrupoIdIn(Collection<String> grupoIds);

    List<SolicitudesGrupos> findByGrupoId(String grupoId);
    
}
