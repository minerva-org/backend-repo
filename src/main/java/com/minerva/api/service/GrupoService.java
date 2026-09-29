package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.GrupoDTO;

public interface GrupoService {
    List<GrupoDTO> findAll();
    List<GrupoDTO> findAllByDocenteId(String docenteId);
    List<GrupoDTO> findAllByPlantelId(Long plantelId);
    GrupoDTO getGrupoById(String grupoId);
    GrupoDTO saveGrupo(GrupoDTO grupoDTO);
    GrupoDTO updateGrupo(String grupoId, GrupoDTO grupoDTO);
    void deleteGrupo(String grupoId);

    List<String> getAlumnosIdsDeGrupo(String grupoId);
    void agregarAlumno(String grupoId, String alumnoId);
    void quitarAlumno(String grupoId, String alumnoId);
}