package com.minerva.api.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.User.User;
import com.minerva.api.dto.PersonaDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Plantel;
import com.minerva.api.User.Roles;
import com.minerva.api.repository.PersonaRepository;
import com.minerva.api.repository.PlantelRepository;
import com.minerva.api.User.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;
    private final PlantelRepository plantelRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.default-password:Cambiar123!}")
    private String defaultPassword;

    @Override
    @Transactional
    public PersonaDTO savePersona(PersonaDTO dto) {
        if (dto.getId() == null || dto.getId().isBlank())
            throw new IllegalArgumentException("El id es obligatorio");
        String id = dto.getId().trim();
        if (personaRepository.existsById(id))
            throw new IllegalArgumentException("Ya existe una persona con ese Id: " + id);

        if (dto.getNombre() == null || dto.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");
        if (dto.getApellido() == null || dto.getApellido().isBlank())
            throw new IllegalArgumentException("El apellido es obligatorio");

        if (dto.getEmail() == null || dto.getEmail().isBlank())
            throw new IllegalArgumentException("El email es obligatorio");
        String email = dto.getEmail().trim();
        if (personaRepository.existsByEmailIgnoreCase(email))
            throw new IllegalArgumentException("Ya existe una persona con ese email: " + email);

        if (dto.getRol() == null)
            throw new IllegalArgumentException("El rol es obligatorio");

        if (dto.getPlantelId() == null)
            throw new IllegalArgumentException("El plantel es obligatorio");
        Plantel plantel = plantelRepository.findById(dto.getPlantelId())
            .orElseThrow(() -> new EntityNotFoundException("Plantel no encontrado con el id: " + dto.getPlantelId()));

        if (dto.getUsername() == null || dto.getUsername().isBlank())
            throw new IllegalArgumentException("El username es obligatorio");
        String username = dto.getUsername().trim();
        if (userRepository.existsByUsernameIgnoreCase(username))
            throw new IllegalArgumentException("Ya existe un usuario con ese username: " + username);

        Persona persona = new Persona();
        persona.setId(id);
        persona.setNombre(dto.getNombre().trim());
        persona.setApellido(dto.getApellido().trim());
        persona.setEmail(email);
        persona.setRol(dto.getRol());
        persona.setPlantel(plantel);
        persona = personaRepository.save(persona);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setReestablecimiento(true);
        user.setPersona(persona);
        userRepository.save(user);

        PersonaDTO resultado = Mapper.toDTO(persona);
        resultado.setUsername(username);
        return resultado;
    }

    @Override
    @Transactional
    public PersonaDTO updatePersona(String personaId, PersonaDTO dto) {
        Persona persona = personaRepository.findById(personaId)
            .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con el id: " + personaId));

        if (dto.getNombre() != null) persona.setNombre(dto.getNombre().trim());
        if (dto.getApellido() != null) persona.setApellido(dto.getApellido().trim());

        if (dto.getEmail() != null) {
            String email = dto.getEmail().trim();
            if (personaRepository.existsByEmailIgnoreCaseAndIdNot(email, personaId))
                throw new IllegalArgumentException("Ya existe una persona con ese email: " + email);
            persona.setEmail(email);
        }

        if (dto.getRol() != null) persona.setRol(dto.getRol());

        if (dto.getPlantelId() != null) {
            Plantel plantel = plantelRepository.findById(dto.getPlantelId())
                .orElseThrow(() -> new EntityNotFoundException("No existe un plantel con el id: " + dto.getPlantelId()));
            persona.setPlantel(plantel);
        }
        // username NO se actualiza aquí — es un cambio de cuenta, no de datos de persona

        PersonaDTO resultado = Mapper.toDTO(personaRepository.save(persona));
        userRepository.findByPersonaId(personaId).ifPresent(u -> resultado.setUsername(u.getUsername()));
        return resultado;
    }

    @Override
    @Transactional
    public void deletePersona(String personaId) {
        Persona persona = personaRepository.findById(personaId)
            .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con el id: " + personaId));
        userRepository.findByPersonaId(personaId).ifPresent(userRepository::delete);
        personaRepository.delete(persona);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaDTO> findAll() {
        return personaRepository.findAll().stream().map(this::toDTOConUsername).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaDTO getPersonaById(String personaId) {
        Persona persona = personaRepository.findById(personaId)
            .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con el id: " + personaId));
        return toDTOConUsername(persona);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaDTO> findAllByPlantelId(String plantelId) {
        if (!plantelRepository.existsById(plantelId))
            throw new EntityNotFoundException("Plantel no encontrado con el id: " + plantelId);
        return personaRepository.findByPlantelId(plantelId).stream().map(this::toDTOConUsername).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaDTO> findAllByRol(Roles rol) {
        return personaRepository.findByRol(rol).stream().map(this::toDTOConUsername).toList();
    }

    private PersonaDTO toDTOConUsername(Persona persona) {
        PersonaDTO dto = Mapper.toDTO(persona);
        userRepository.findByPersonaId(persona.getId()).ifPresent(u -> dto.setUsername(u.getUsername()));
        return dto;
    }
}