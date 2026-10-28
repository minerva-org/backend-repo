package com.minerva.api.service;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.User.User;
import com.minerva.api.dto.AlumnoDTO;
import com.minerva.api.dto.DocenteDTO;
import com.minerva.api.dto.PersonaRequestDTO;
import com.minerva.api.dto.PersonaResponseDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Alumno;
import com.minerva.api.model.Docente;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Plantel;
import com.minerva.api.User.Roles;
import com.minerva.api.repository.AlumnoRepository;
import com.minerva.api.repository.DocenteRepository;
import com.minerva.api.repository.PersonaRepository;
import com.minerva.api.repository.PlantelRepository;
import com.minerva.api.User.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {

    private static final Set<Roles> ROLES_CON_PASSWORD_PERSONAL = Set.of(
            Roles.DOCENTE,
            Roles.COORDINADOR,
            Roles.DIRECTOR_PLANTEL,
            Roles.ALUMNO);

    private final PersonaRepository personaRepository;
    private final PlantelRepository plantelRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DocenteRepository docenteRepository;
    private final AlumnoRepository alumnoRepository;

    @Value("${app.security.default-password:Cambiar123!}")
    private String defaultPassword;

    @Override
    @Transactional
    public PersonaResponseDTO savePersona(PersonaRequestDTO dto) {
        Persona persona = new Persona();
        persona.setRol(dto.getRol()); // aquí sí viene del cliente
        return Mapper.toResponse(guardarBase(persona, dto));
    }

    private Persona guardarBase(Persona persona, PersonaRequestDTO dto) {
        if (dto.getId() == null || dto.getId().isBlank())
            throw new IllegalArgumentException("El id es obligatorio");
        String id = dto.getId().trim();
        if (personaRepository.existsById(id))
            throw new IllegalArgumentException("Ya existe una persona con ese Id: " + id);

        if (dto.getNombre() == null || dto.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");
        if (dto.getApellidoMaterno() == null || dto.getApellidoMaterno().isBlank())
            throw new IllegalArgumentException("El apellido Materno es obligatorio");

        if (dto.getEmail() == null || dto.getEmail().isBlank())
            throw new IllegalArgumentException("El email es obligatorio");
        String email = dto.getEmail().trim();
        if (personaRepository.existsByEmailIgnoreCase(email))
            throw new IllegalArgumentException("Ya existe una persona con ese email: " + email);

        // El rol ya lo asignó quien llamó (saveAlumno, saveDocente o savePersona)
        if (persona.getRol() == null)
            throw new IllegalArgumentException("El rol es obligatorio");

        if (dto.getPlantelId() == null)
            throw new IllegalArgumentException("El plantel es obligatorio");
        Plantel plantel = plantelRepository.findById(dto.getPlantelId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Plantel no encontrado con el id: " + dto.getPlantelId()));

        boolean passwordPersonal = ROLES_CON_PASSWORD_PERSONAL.contains(persona.getRol());
        String rawPassword = passwordPersonal ? dto.getPassword() : defaultPassword;
        if (passwordPersonal && (rawPassword == null || rawPassword.isBlank()))
            throw new IllegalArgumentException("La contraseña es obligatoria para este rol");

        // OJO: NO hay "new Persona()" aquí, se usa la que llegó por parámetro
        persona.setId(id);
        persona.setNombre(dto.getNombre().trim());
        persona.setApellidoMaterno(dto.getApellidoMaterno().trim());
        if (dto.getApellidoPaterno() != null && !dto.getApellidoPaterno().isBlank())
            persona.setApellidoPaterno(dto.getApellidoPaterno().trim());
        persona.setEmail(email);
        persona.setActivo(dto.getActivo() == null ? true : dto.getActivo());
        persona.setPlantel(plantel);
        persona.setPassword(passwordEncoder.encode(rawPassword)); // requiere campo password en Persona

        return personaRepository.save(persona);
    }

    @Override
    @Transactional
    public AlumnoDTO saveAlumno(AlumnoDTO dto) {
        Alumno alumno = new Alumno();
        alumno.setRol(Roles.ALUMNO);
        alumno.setNombreTutor(dto.getNombreTutor());
        alumno.setNumeroTutor(dto.getNumeroTutor());
        alumno.setPromedio(dto.getPromedio());
        if (dto.getFechaIngreso() != null)
            alumno.setFechaIngreso(dto.getFechaIngreso());
        return Mapper.toDTO((Alumno) guardarBase(alumno, dto));
    }

    @Override
    @Transactional
    public DocenteDTO saveDocente(DocenteDTO dto) {
        Docente docente = new Docente();
        docente.setRol(Roles.DOCENTE);
        docente.setCarrera(dto.getCarrera());
        docente.setTituloEdu(dto.getTituloEdu());
        return Mapper.toDTO((Docente) guardarBase(docente, dto));
    }

    @Override
    @Transactional
    public PersonaResponseDTO updatePersona(String personaId, PersonaRequestDTO dto) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con el id: " + personaId));
        actualizarBase(persona, dto);
        return Mapper.toResponse(persona);
    }

    @Override
    @Transactional
    public AlumnoDTO updateAlumno(String id, AlumnoDTO dto) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado con el id: " + id));
        actualizarBase(alumno, dto);

        if (dto.getNombreTutor() != null)
            alumno.setNombreTutor(dto.getNombreTutor().trim());
        if (dto.getNumeroTutor() != null)
            alumno.setNumeroTutor(dto.getNumeroTutor().trim());
        if (dto.getFechaIngreso() != null)
            alumno.setFechaIngreso(dto.getFechaIngreso());
        if (dto.getPromedio() != null)
            alumno.setPromedio(dto.getPromedio());

        return Mapper.toDTO(alumno);
    }

    @Override
    @Transactional
    public DocenteDTO updateDocente(String id, DocenteDTO dto) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado con el id: " + id));
        actualizarBase(docente, dto);

        if (dto.getCarrera() != null)
            docente.setCarrera(dto.getCarrera().trim());
        if (dto.getTituloEdu() != null)
            docente.setTituloEdu(dto.getTituloEdu().trim());

        return Mapper.toDTO(docente);
    }

    private void actualizarBase(Persona persona, PersonaRequestDTO dto) {
        if (dto.getNombre() != null) {
            if (dto.getNombre().isBlank())
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            persona.setNombre(dto.getNombre().trim());
        }

        if (dto.getApellidoMaterno() != null) {
            if (dto.getApellidoMaterno().isBlank())
                throw new IllegalArgumentException("El apellido materno no puede estar vacío");
            persona.setApellidoMaterno(dto.getApellidoMaterno().trim());
        }

        if (dto.getApellidoPaterno() != null) {
            String paterno = dto.getApellidoPaterno().trim();
            persona.setApellidoPaterno(paterno.isEmpty() ? null : paterno);
        }

        if (dto.getEmail() != null) {
            String email = dto.getEmail().trim();
            if (email.isEmpty())
                throw new IllegalArgumentException("El email no puede estar vacío");
            if (personaRepository.existsByEmailIgnoreCaseAndIdNot(email, persona.getId()))
                throw new IllegalArgumentException("Ya existe una persona con ese email: " + email);
            persona.setEmail(email);
        }

        if (dto.getRol() != null && dto.getRol() != persona.getRol()) {
            if (persona instanceof Alumno || persona instanceof Docente)
                throw new IllegalArgumentException("No se puede cambiar el rol de un alumno o docente");
            persona.setRol(dto.getRol());
        }

        if (dto.getActivo() != null)
            persona.setActivo(dto.getActivo());

        if (dto.getPlantelId() != null) {
            Plantel plantel = plantelRepository.findById(dto.getPlantelId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No existe un plantel con el id: " + dto.getPlantelId()));
            persona.setPlantel(plantel);
        }
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
    public List<PersonaResponseDTO> findAll() {
        return personaRepository.findAll().stream().map(Mapper::toResponse).toList();
    }

    public List<PersonaResponseDTO> findAllByPlantelIdAndRolAndStatus(long plantelid, Roles rol, Boolean activo) {
        if (activo != null) {
            return personaRepository.findByPlantelIdAndRolAndActivo(plantelid, rol, activo)
                    .stream()
                    .map(Mapper::toResponse)
                    .toList();
        }
        return personaRepository.findByPlantelIdAndRol(plantelid, rol)
                .stream()
                .map(Mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResponseDTO getPersonaById(String personaId) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con el id: " + personaId));
        return Mapper.toResponse(persona);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponseDTO> findAllByPlantelId(Long plantelId) {
        if (!plantelRepository.existsById(plantelId))
            throw new EntityNotFoundException("Plantel no encontrado con el id: " + plantelId);
        return personaRepository.findByPlantelId(plantelId).stream().map(Mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponseDTO> findAllDocentesByPlantelId(Long plantelId) {
        if (!plantelRepository.existsById(plantelId)) {
            throw new EntityNotFoundException("Plantel no encontrado con el id: " + plantelId);
        }

        return personaRepository.findByPlantelId(plantelId)
                .stream()
                .filter(persona -> persona.getRol() == Roles.DOCENTE || persona.getRol() == Roles.COORDINADOR)
                .map(Mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponseDTO> findAllByPlantelIdAndRol(Long plantelId, Roles rol) {
        if (!plantelRepository.existsById(plantelId)) {
            throw new EntityNotFoundException("Plantel no encontrado con el id: " + plantelId);
        }
        return personaRepository.findByPlantelIdAndRol(plantelId, rol)
                .stream()
                .map(Mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponseDTO> findAllByRol(Roles rol) {
        return personaRepository.findByRol(rol).stream().map(Mapper::toResponse).toList();
    }

    // private PersonaResponseDTO toDTOConUsername(Persona persona) {
    // PersonaDTO dto = Mapper.toDTO(persona);
    // userRepository.findByPersonaId(persona.getId()).ifPresent(u ->
    // dto.setUsername(u.getUsername()));
    // return dto;
    // }

    private String generateUniqueUsernameFromEmail(String email) {
        String base = email.split("@")[0].trim().toLowerCase();
        String sanitized = base.replaceAll("[^a-z0-9._-]", "");
        String candidate = sanitized.isBlank() ? "usuario" : sanitized;

        int suffix = 1;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            candidate = sanitized + suffix;
            suffix++;
        }

        return candidate;
    }
}