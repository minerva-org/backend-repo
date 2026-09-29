package com.minerva.api.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.GrupoDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Plantel;
import com.minerva.api.User.Roles;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.PersonaRepository;
import com.minerva.api.repository.PlantelRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GrupoServiceImpl implements GrupoService {

    private final GrupoRepository grupoRepository;
    private final PersonaRepository personaRepository;
    private final PlantelRepository plantelRepository;

    private boolean esDocenteValido(Persona persona) {
        return persona.getRol() == Roles.DOCENTE || persona.getRol() == Roles.COORDINADOR;
    }

    @Override
    @Transactional
    public GrupoDTO saveGrupo(GrupoDTO grupoDTO) {
        if (grupoDTO.getId() == null || grupoDTO.getId().isBlank()) {
            throw new IllegalArgumentException("El id es obligatorio");
        }
        String id = grupoDTO.getId().trim();
        if (grupoRepository.existsById(id)) {
            throw new IllegalArgumentException("Ya existe un grupo con ese Id: " + id);
        }

        if (grupoDTO.getClaveGrupo() == null || grupoDTO.getClaveGrupo().isBlank()) {
            throw new IllegalArgumentException("La clave de grupo es obligatoria");
        }
        String claveGrupo = grupoDTO.getClaveGrupo().trim();
        if (grupoRepository.existsByClaveGrupoIgnoreCase(claveGrupo)) {
            throw new IllegalArgumentException("Ya existe un grupo con esa clave: " + claveGrupo);
        }

        if (grupoDTO.getNombre() == null || grupoDTO.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        String nombre = grupoDTO.getNombre().trim();

        if (grupoDTO.getSemestre() == null || grupoDTO.getSemestre().isBlank()) {
            throw new IllegalArgumentException("El semestre es obligatorio");
        }
        String semestre = grupoDTO.getSemestre().trim();

        if (grupoDTO.getDocenteId() == null || grupoDTO.getDocenteId().isBlank()) {
            throw new IllegalArgumentException("El docente es obligatorio");
        }
        String docenteId = grupoDTO.getDocenteId().trim();
        Persona docente = personaRepository.findById(docenteId)
            .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con el id: " + docenteId));
        if (!esDocenteValido(docente)) {
            throw new IllegalArgumentException("La persona indicada no tiene un rol de docente o coordinador");
        }

        if (grupoDTO.getPlantelId() == null) {
            throw new IllegalArgumentException("El plantel es obligatorio");
        }
        Plantel plantel = plantelRepository.findById(grupoDTO.getPlantelId())
            .orElseThrow(() -> new EntityNotFoundException("Plantel no encontrado con el id: " + grupoDTO.getPlantelId()));

        Grupo grupo = new Grupo();
        grupo.setId(id);
        grupo.setClaveGrupo(claveGrupo);
        grupo.setNombre(nombre);
        grupo.setSemestre(semestre);
        grupo.setActivo(grupoDTO.getActivo() != null ? grupoDTO.getActivo() : true);
        grupo.setDocente(docente);
        grupo.setPlantel(plantel);

        if (grupoDTO.getAlumnosIds() != null) {
            for (String alumnoId : grupoDTO.getAlumnosIds()) {
                if (alumnoId == null || alumnoId.isBlank()) continue;
                Persona alumno = personaRepository.findById(alumnoId.trim())
                    .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con el id: " + alumnoId));
                if (alumno.getRol() != Roles.ALUMNO) {
                    throw new IllegalArgumentException("La persona indicada no tiene rol de alumno");
                }
                grupo.addAlumno(alumno);
            }
        }

        return Mapper.toDTO(grupoRepository.save(grupo));
    }

    @Override
    @Transactional
    public GrupoDTO updateGrupo(String grupoId, GrupoDTO grupoDTO) {
        Grupo grupo = grupoRepository.findById(grupoId)
            .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado con el id: " + grupoId));

        if (grupoDTO.getClaveGrupo() != null) {
            String claveGrupo = grupoDTO.getClaveGrupo().trim();
            if (claveGrupo.isEmpty()) {
                throw new IllegalArgumentException("La clave de grupo no puede estar vacía");
            }
            if (grupoRepository.existsByClaveGrupoIgnoreCaseAndIdNot(claveGrupo, grupoId)) {
                throw new IllegalArgumentException("Ya existe un grupo con esa clave: " + claveGrupo);
            }
            grupo.setClaveGrupo(claveGrupo);
        }

        if (grupoDTO.getNombre() != null) {
            String nombre = grupoDTO.getNombre().trim();
            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
            grupo.setNombre(nombre);
        }

        if (grupoDTO.getSemestre() != null) {
            String semestre = grupoDTO.getSemestre().trim();
            if (semestre.isEmpty()) {
                throw new IllegalArgumentException("El semestre no puede estar vacío");
            }
            grupo.setSemestre(semestre);
        }

        if (grupoDTO.getActivo() != null) {
            grupo.setActivo(grupoDTO.getActivo());
        }

        if (grupoDTO.getDocenteId() != null) {
            String docenteId = grupoDTO.getDocenteId().trim();
            Persona docente = personaRepository.findById(docenteId)
                .orElseThrow(() -> new EntityNotFoundException("No existe una persona con el id: " + docenteId));
            if (!esDocenteValido(docente)) {
                throw new IllegalArgumentException("La persona indicada no tiene un rol de docente o coordinador");
            }
            grupo.setDocente(docente);
        }

        if (grupoDTO.getPlantelId() != null) {
            Plantel plantel = plantelRepository.findById(grupoDTO.getPlantelId())
                .orElseThrow(() -> new EntityNotFoundException("Plantel no encontrado con el id: " + grupoDTO.getPlantelId()));
            grupo.setPlantel(plantel);
        }

        if (grupoDTO.getAlumnosIds() != null) {
            for (String alumnoId : grupoDTO.getAlumnosIds()) {
                if (alumnoId == null || alumnoId.isBlank()) continue;
                Persona alumno = personaRepository.findById(alumnoId.trim())
                    .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con el id: " + alumnoId));
                if (alumno.getRol() != Roles.ALUMNO) {
                    throw new IllegalArgumentException("La persona indicada no tiene rol de alumno");
                }
                grupo.addAlumno(alumno);
            }
        }

        return Mapper.toDTO(grupoRepository.save(grupo));
    }

    @Override
    @Transactional
    public void deleteGrupo(String grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
            .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado con el id: " + grupoId));

        grupoRepository.delete(grupo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoDTO> findAll() {
        return grupoRepository.findAll()
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GrupoDTO getGrupoById(String grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
            .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado con el id: " + grupoId));

        return Mapper.toDTO(grupo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoDTO> findAllByDocenteId(String docenteId) {
        if (!personaRepository.existsById(docenteId)) {
            throw new EntityNotFoundException("Persona no encontrada con el id: " + docenteId);
        }
        return grupoRepository.findByDocenteId(docenteId)
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoDTO> findAllByPlantelId(Long plantelId) {
        if (plantelId == null) {
            return findAll();
        }
        return grupoRepository.findByPlantelId(plantelId)
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAlumnosIdsDeGrupo(String grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
            .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado con el id: " + grupoId));

        return grupo.getAlumnos().stream()
            .map(Persona::getId)
            .toList();
    }

    @Override
    @Transactional
    public void agregarAlumno(String grupoId, String alumnoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
            .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado con el id: " + grupoId));
        Persona alumno = personaRepository.findById(alumnoId)
            .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con el id: " + alumnoId));

        if (alumno.getRol() != Roles.ALUMNO) {
            throw new IllegalArgumentException("La persona indicada no tiene rol de alumno");
        }
        if (grupo.getAlumnos().contains(alumno)) {
            throw new IllegalArgumentException("El alumno ya pertenece a este grupo");
        }
        grupo.addAlumno(alumno);
        grupoRepository.save(grupo);
    }

    @Override
    @Transactional
    public void quitarAlumno(String grupoId, String alumnoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
            .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado con el id: " + grupoId));
        Persona alumno = personaRepository.findById(alumnoId)
            .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con el id: " + alumnoId));

        if (!grupo.getAlumnos().contains(alumno)) {
            throw new IllegalArgumentException("El alumno no pertenece a este grupo");
        }
        grupo.removeAlumno(alumno);
        grupoRepository.save(grupo);
    }
}