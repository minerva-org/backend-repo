package com.minerva.api.service;

import java.util.List;
import com.minerva.api.dto.PersonaDTO;
import com.minerva.api.User.Roles;

public interface PersonaService {
    List<PersonaDTO> findAll();

    List<PersonaDTO> findAllByPlantelId(Long plantelId);

    List<PersonaDTO> findAllDocentesByPlantelId(Long plantelId);

    List<PersonaDTO> findAllByPlantelIdAndRol(Long plantelId, Roles rol);

    List<PersonaDTO> findAllByRol(Roles rol);

    PersonaDTO getPersonaById(String personaId);

    PersonaDTO savePersona(PersonaDTO dto);

    PersonaDTO updatePersona(String personaId, PersonaDTO dto);
    
    void deletePersona(String personaId);
}