package com.minerva.api.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.SolicitudesGruposRequestDTO;
import com.minerva.api.dto.SolicitudesGruposResponseDTO;
import com.minerva.api.mapper.MapperSolicitudesGrupos;
import com.minerva.api.model.Alumno;
import com.minerva.api.model.Docente;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Persona;
import com.minerva.api.model.SolicitudesGrupos;
import com.minerva.api.repository.AlumnoRepository;
import com.minerva.api.repository.DocenteRepository;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.PersonaRepository;
import com.minerva.api.repository.SolicitudesGruposRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SolicitudesGruposeService {

    private final AlumnoRepository alumnoRepository;
    private final PersonaRepository personaRepository;
    private final GrupoRepository grupoRepository;
    private final SolicitudesGruposRepository solicitudesRepository;
        private final GrupoService grupoService;

    public SolicitudesGruposResponseDTO saveSolicitud(SolicitudesGruposRequestDTO solicitud) {
        if (solicitud == null)
            return null;

        if (solicitud.alumnoId() == null || solicitud.alumnoId().isBlank()) {
            throw new IllegalArgumentException("El id del alumno es obligatorio");
        }

        if (solicitud.grupoId() == null || solicitud.grupoId().isBlank()) {
            throw new IllegalArgumentException("El id del grupo es obligatorio");
        }

        if (solicitudesRepository.existsByAlumnoIdAndGrupoId(solicitud.alumnoId(), solicitud.grupoId())) {
            throw new IllegalArgumentException("Ya existe una solicitud activa de este alumno para este grupo");
        }

        Alumno alumno = alumnoRepository.findById(solicitud.alumnoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró un alumno con este id: " + solicitud.alumnoId()));

        Grupo grupo = grupoRepository.findById(solicitud.grupoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró un grupo con este id: " + solicitud.grupoId()));

        SolicitudesGrupos nuevaSolicitud = new SolicitudesGrupos();
        nuevaSolicitud.setAlumnoId(alumno);
        nuevaSolicitud.setGrupoId(grupo);

        solicitudesRepository.save(nuevaSolicitud);
        return MapperSolicitudesGrupos.toDTO(nuevaSolicitud, alumno, grupo);
    }

    public List<SolicitudesGruposResponseDTO> findAllByGrupoId(String grupoId) {
        if (grupoId == null || grupoId.isBlank()) {
            throw new IllegalArgumentException("El id del grupo es obligatorio");
        }

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(
                        () -> new EntityNotFoundException("No se encontró un grupo con el siguiente id: " + grupoId));

        List<SolicitudesGrupos> solicitudes = solicitudesRepository.findByGrupoId(grupoId);

        return solicitudes.stream()
                .map(solicitud -> MapperSolicitudesGrupos.toDTO(solicitud, solicitud.getAlumnoId(), grupo))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudesGruposResponseDTO> findByDocenteId(String docenteId) {
        if (docenteId == null || docenteId.isBlank()) {
            throw new IllegalArgumentException("El docente es obligatorio");
        }

        Persona persona = personaRepository.findById(docenteId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró un docente con este id: " + docenteId));

        List<Grupo> grupos = grupoRepository.findByDocenteId(persona.getId());
        if (grupos.isEmpty()) {
            return List.of();
        }

        // Mapa de id - grupo
        Map<String, Grupo> gruposPorId = grupos.stream()
                .collect(Collectors.toMap(Grupo::getId, Function.identity()));

        List<SolicitudesGrupos> solicitudes = solicitudesRepository.findByGrupoIdIn(gruposPorId.keySet());
        if (solicitudes.isEmpty()) {
            return List.of();
        }

        List<String> alumnoIds = solicitudes.stream()
                .map(SolicitudesGrupos::getAlumnoId)
                .filter(Objects::nonNull)
                .map(Alumno::getId)
                .distinct()
                .toList();

        Map<String, Alumno> alumnosPorId = alumnoRepository.findAllById(alumnoIds).stream()
                .collect(Collectors.toMap(Alumno::getId, Function.identity()));

        return solicitudes.stream()
                .map(solicitud -> {
                    Alumno alumno = solicitud.getAlumnoId();
                    Alumno alumnoResuelto = (alumno != null) ? alumnosPorId.get(alumno.getId()) : null;
                    return MapperSolicitudesGrupos.toDTO(
                            solicitud,
                            alumnoResuelto,
                            gruposPorId.get(solicitud.getGrupoId()));
                })
                .toList();
    }

    public List<SolicitudesGruposResponseDTO> findAll() {
        List<SolicitudesGrupos> solicitudes = solicitudesRepository.findAll();

        return solicitudes.stream()
                .map(solicitud -> MapperSolicitudesGrupos.toDTO(solicitud, solicitud.getAlumnoId(),
                        solicitud.getGrupoId()))
                .toList();
    }

    public void aceptar(String solicitudId, String docenteId){
        if(solicitudId == null || solicitudId.isBlank())
            throw new IllegalArgumentException("El id de la solicitud es obligatorio");

        if(docenteId == null || docenteId.isBlank())
            throw new IllegalArgumentException("El id del docente es obligatorio");

        Persona docente = personaRepository.findById(docenteId)
            .orElseThrow(() -> new EntityNotFoundException("No se encontró un docente con este id: " + docenteId));

        SolicitudesGrupos solicitud = solicitudesRepository.findById(solicitudId)
            .orElseThrow(() -> new EntityNotFoundException("No se encontró una solicitud con este id: " + docenteId));

        Alumno alumno = alumnoRepository.findById(solicitud.getAlumnoId().getId())
            .orElseThrow(() -> new EntityNotFoundException("No se encontró un alumno con este id: " + docenteId));

        Grupo grupo = grupoRepository.findById(solicitud.getGrupoId().getId())
            .orElseThrow(() -> new EntityNotFoundException("No se encontró un grupo con este id: " + solicitud.getGrupoId().getId()));

        if(grupo.getDocente().getId() != docente.getId()){
            throw new IllegalArgumentException("No puede aceptar solicitudes de un grupo al que no pertenece");
        }
        grupoService.agregarAlumno(grupo.getId(), alumno.getId());
        solicitudesRepository.delete(solicitud);
    }

        public void rechazar(String solicitudId, String docenteId){
        if(solicitudId == null || solicitudId.isBlank())
            throw new IllegalArgumentException("El id de la solicitud es obligatorio");

        if(docenteId == null || docenteId.isBlank())
            throw new IllegalArgumentException("El id del docente es obligatorio");

        Persona docente = personaRepository.findById(docenteId)
            .orElseThrow(() -> new EntityNotFoundException("No se encontró un docente con este id: " + docenteId));

        SolicitudesGrupos solicitud = solicitudesRepository.findById(solicitudId)
            .orElseThrow(() -> new EntityNotFoundException("No se encontró una solicitud con este id: " + docenteId));

        if(!alumnoRepository.existsById(solicitud.getAlumnoId().getId()))
            throw new EntityNotFoundException("No se encontró un alumno con este id: " + solicitud.getAlumnoId().getId());
        
        Grupo grupo = grupoRepository.findById(solicitud.getGrupoId().getId())
            .orElseThrow(() -> new EntityNotFoundException("No se encontró un grupo con este id: " + solicitud.getGrupoId().getId()));

        if(grupo.getDocente().getId() != docente.getId()){
            throw new IllegalArgumentException("No puede descartar solicitudes de un grupo al que no pertenece");
        }
        solicitudesRepository.delete(solicitud);
    }
}
