package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.PersonaDatosCompletosResponseDTO;
import com.minerva.api.dto.PersonaRequestDTO;
import com.minerva.api.dto.PersonaResponseDTO;
import com.minerva.api.User.Roles;

public interface PersonaService {
    List<PersonaResponseDTO> findAll();

    List<PersonaDatosCompletosResponseDTO> findAllDatos();

    List<PersonaResponseDTO> findAllByPlantelId(Long plantelId);

    List<PersonaResponseDTO> findAllByPlantelIdAndRolAndStatus(long plantelid, Roles rol, Boolean activo);

    List<PersonaResponseDTO> findAllDocentesByPlantelId(Long plantelId);

    List<PersonaResponseDTO> findAllByPlantelIdAndRol(Long plantelId, Roles rol);

    List<PersonaResponseDTO> findAllByRol(Roles rol);

    PersonaDatosCompletosResponseDTO getDirectorPlantelByPlantelId(Long plantelId);

    PersonaResponseDTO getPersonaById(String personaId);

    PersonaResponseDTO savePersona(PersonaRequestDTO dto);

    PersonaResponseDTO updatePersona(String personaId, PersonaRequestDTO dto);
    
    void deletePersona(String personaId);
}