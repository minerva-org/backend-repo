package com.minerva.api.service;

import java.util.Collection;
import java.util.List;

import com.minerva.api.dto.PersonaDatosCompletosResponseDTO;
import com.minerva.api.dto.PersonaRequestDTO;
import com.minerva.api.dto.PersonaResponseDTO;
import com.minerva.api.User.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.minerva.api.User.User;

public interface PersonaService {
    List<PersonaResponseDTO> findAll();

    List<PersonaResponseDTO> findAll(User usuario);

    PersonaResponseDTO getAuthenticatedPersona(User usuario);

    List<PersonaDatosCompletosResponseDTO> findAllDatos();

    List<PersonaResponseDTO> findAllByPlantelId(Long plantelId);

    List<PersonaResponseDTO> findAllByPlantelId(Long plantelId, User usuario);

    List<PersonaResponseDTO> findAllByPlantelIdAndRolAndStatus(long plantelid, Roles rol, Boolean activo);

    List<PersonaResponseDTO> findAllByPlantelIdAndRolAndStatus(long plantelId, Roles rol, Boolean activo, User usuario);

    List<PersonaResponseDTO> findAllDocentesByPlantelId(Long plantelId);

    List<PersonaResponseDTO> findAllByPlantelIdAndRol(Long plantelId, Roles rol);

    List<PersonaResponseDTO> findAllByRol(Roles rol);

    List<PersonaResponseDTO> findAllByRol(Roles rol, User usuario);

    Page<PersonaResponseDTO> findPaginatedPeople(Long plantelId, Collection<Roles> roles, Boolean activo, String search,
            Pageable pageable);

    Page<PersonaResponseDTO> findPaginatedPeople(Long plantelId, Collection<Roles> roles, Boolean activo, String search,
            Pageable pageable, User usuario);

    PersonaDatosCompletosResponseDTO getDirectorPlantelByPlantelId(Long plantelId);

    PersonaResponseDTO getPersonaById(String personaId);

    PersonaResponseDTO savePersona(PersonaRequestDTO dto);

    PersonaResponseDTO updatePersona(String personaId, PersonaRequestDTO dto);

    void deletePersona(String personaId);
}