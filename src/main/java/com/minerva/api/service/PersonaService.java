package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.PersonaDatosCompletosResponseDTO;
import com.minerva.api.dto.PersonaRequestDTO;
import com.minerva.api.dto.PersonaResponseDTO;
import com.minerva.api.User.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PersonaService {
    List<PersonaResponseDTO> findAll();

    List<PersonaDatosCompletosResponseDTO> findAllDatos();

    List<PersonaResponseDTO> findAllByPlantelId(Long plantelId);

    List<PersonaResponseDTO> findAllByPlantelIdAndRolAndStatus(long plantelid, Roles rol, Boolean activo);

    List<PersonaResponseDTO> findAllDocentesByPlantelId(Long plantelId);

    List<PersonaResponseDTO> findAllByPlantelIdAndRol(Long plantelId, Roles rol);

    List<PersonaResponseDTO> findAllByRol(Roles rol);

    Page<PersonaResponseDTO> findPaginatedPeople(Long plantelId, Roles rol, Boolean activo, String search,
            Pageable pageable);

    PersonaDatosCompletosResponseDTO getDirectorPlantelByPlantelId(Long plantelId);

    PersonaResponseDTO getPersonaById(String personaId);

    PersonaResponseDTO savePersona(PersonaRequestDTO dto);

    PersonaResponseDTO updatePersona(String personaId, PersonaRequestDTO dto);

    void deletePersona(String personaId);
}