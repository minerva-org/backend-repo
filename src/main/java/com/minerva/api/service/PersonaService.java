package com.minerva.api.service;

import java.util.List;
import com.minerva.api.dto.PersonaDTO;
import com.minerva.api.dto.PersonaResponseDTO;
import com.minerva.api.User.Roles;

public interface PersonaService {
    List<PersonaResponseDTO> findAll();

    List<PersonaResponseDTO> findAllByPlantelId(Long plantelId);

    List<PersonaResponseDTO> findAllByPlantelIdAndRolAndStatus(long plantelid, Roles rol, Boolean activo);

    List<PersonaResponseDTO> findAllDocentesByPlantelId(Long plantelId);

    List<PersonaResponseDTO> findAllByPlantelIdAndRol(Long plantelId, Roles rol);

    List<PersonaResponseDTO> findAllByRol(Roles rol);

    PersonaResponseDTO getPersonaById(String personaId);

    PersonaResponseDTO savePersona(PersonaDTO dto);

    PersonaResponseDTO updatePersona(String personaId, PersonaDTO dto);
    
    void deletePersona(String personaId);
}