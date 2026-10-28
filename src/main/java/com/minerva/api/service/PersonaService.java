package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.AlumnoDTO;
import com.minerva.api.dto.DocenteDTO;
import com.minerva.api.dto.PersonaRequestDTO;
import com.minerva.api.dto.PersonaResponseDTO;
import com.minerva.api.model.Persona;
import com.minerva.api.User.Roles;

public interface PersonaService {
    List<PersonaResponseDTO> findAll();

    List<PersonaResponseDTO> findAllByPlantelId(Long plantelId);

    List<PersonaResponseDTO> findAllByPlantelIdAndRolAndStatus(long plantelid, Roles rol, Boolean activo);

    List<PersonaResponseDTO> findAllDocentesByPlantelId(Long plantelId);

    List<PersonaResponseDTO> findAllByPlantelIdAndRol(Long plantelId, Roles rol);

    List<PersonaResponseDTO> findAllByRol(Roles rol);

    PersonaResponseDTO getPersonaById(String personaId);

    PersonaResponseDTO savePersona(PersonaRequestDTO dto);

    DocenteDTO saveDocente(DocenteDTO dto);

    AlumnoDTO saveAlumno(AlumnoDTO dto);

    PersonaResponseDTO updatePersona(String personaId, PersonaRequestDTO dto);

    DocenteDTO updateDocente(String id, DocenteDTO dto);

    AlumnoDTO updateAlumno(String id, AlumnoDTO dto);
    
    void deletePersona(String personaId);
}