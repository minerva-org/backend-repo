package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.AlumnoGrupoDTO;
import com.minerva.api.dto.GrupoDTO;
import com.minerva.api.User.User;

public interface GrupoService {
    List<GrupoDTO> findAll();

    List<GrupoDTO> findAllByDocenteId(String docenteId);

    List<GrupoDTO> findAllByPlantelId(Long plantelId);

    List<GrupoDTO> findAllByAlumnoId(String alumnoId);
    
    GrupoDTO getGrupoById(String grupoId);

    GrupoDTO saveGrupo(GrupoDTO grupoDTO);

    GrupoDTO saveGrupo(GrupoDTO grupoDTO, User usuario);

    GrupoDTO updateGrupo(String grupoId, GrupoDTO grupoDTO);

    void deleteGrupo(String grupoId);

    List<String> getAlumnosIdsDeGrupo(String grupoId);

    List<AlumnoGrupoDTO> getAlumnosDetalleDeGrupo(String grupoId);

    void agregarAlumno(String grupoId, String alumnoId);

    void quitarAlumno(String grupoId, String alumnoId);
}