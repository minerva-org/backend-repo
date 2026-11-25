package com.minerva.api.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.SolicitudesGruposResponseDTO;
import com.minerva.api.excepciones.ConflictoSolicitudException;
import com.minerva.api.mapper.MapperSolicitudesGrupos;
import com.minerva.api.model.Alumno;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.SolicitudesGrupos;
import com.minerva.api.repository.AlumnoRepository;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.SolicitudesGruposRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SolicitudesGrupoService {

    private final AlumnoRepository alumnoRepository;
    private final GrupoRepository grupoRepository;
    private final SolicitudesGruposRepository solicitudesRepository;
    private final GrupoService grupoService;

    @Transactional
    public SolicitudesGruposResponseDTO saveSolicitud(String grupoId, String alumnoId) {
        if (grupoId == null || grupoId.isBlank()) {
            throw new IllegalArgumentException("El id del grupo es obligatorio");
        }
        if (alumnoId == null || alumnoId.isBlank()) {
            throw new IllegalArgumentException("El id del alumno es obligatorio");
        }

        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró un alumno con este id: " + alumnoId));

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró un grupo con este id: " + grupoId));

        if (!grupo.isActivo()) {
            throw new IllegalArgumentException("El grupo no está activo");
        }
        if (grupoRepository.existsByIdAndAlumnos_Id(grupoId, alumnoId)) {
            throw new ConflictoSolicitudException("Ya perteneces a este grupo");
        }
        if (solicitudesRepository.existsByAlumnoIdAndGrupoId(alumnoId, grupoId)) {
            throw new ConflictoSolicitudException("Ya existe una solicitud pendiente para este grupo");
        }

        SolicitudesGrupos nueva = new SolicitudesGrupos();
        nueva.setAlumnoId(alumnoId);
        nueva.setGrupoId(grupoId);
        solicitudesRepository.save(nueva);

        return MapperSolicitudesGrupos.toDTO(nueva, alumno, grupo);
    }

    @Transactional(readOnly = true)
    public List<SolicitudesGruposResponseDTO> findAllByGrupoId(String grupoId) {
        if (grupoId == null || grupoId.isBlank()) {
            throw new IllegalArgumentException("El id del grupo es obligatorio");
        }

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró un grupo con el siguiente id: " + grupoId));

        return convertir(solicitudesRepository.findByGrupoId(grupoId), Map.of(grupoId, grupo));
    }

    @Transactional(readOnly = true)
    public List<SolicitudesGruposResponseDTO> findByDocenteId(String docenteId) {
        if (docenteId == null || docenteId.isBlank()) {
            throw new IllegalArgumentException("El docente es obligatorio");
        }

        List<Grupo> grupos = grupoRepository.findByDocenteId(docenteId);
        if (grupos.isEmpty()) {
            return List.of();
        }

        Map<String, Grupo> gruposPorId = grupos.stream()
                .collect(Collectors.toMap(Grupo::getId, Function.identity()));

        return convertir(solicitudesRepository.findByGrupoIdIn(gruposPorId.keySet()), gruposPorId);
    }

    @Transactional(readOnly = true)
    public List<SolicitudesGruposResponseDTO> findAll() {
        Map<String, Grupo> gruposPorId = grupoRepository.findAll().stream()
                .collect(Collectors.toMap(Grupo::getId, Function.identity()));

        return convertir(solicitudesRepository.findAll(), gruposPorId);
    }

    @Transactional
    public void aceptar(String solicitudId, String docenteId) {
        SolicitudesGrupos solicitud = cargarSolicitudDelDocente(solicitudId, docenteId,
                "No puede aceptar solicitudes de un grupo al que no pertenece");

        grupoService.agregarAlumno(solicitud.getGrupoId(), solicitud.getAlumnoId());
        solicitudesRepository.delete(solicitud);
    }

    @Transactional
    public void rechazar(String solicitudId, String docenteId) {
        SolicitudesGrupos solicitud = cargarSolicitudDelDocente(solicitudId, docenteId,
                "No puede descartar solicitudes de un grupo al que no pertenece");

        solicitudesRepository.delete(solicitud);
    }

    private List<SolicitudesGruposResponseDTO> convertir(
            List<SolicitudesGrupos> solicitudes, Map<String, Grupo> gruposPorId) {

        if (solicitudes.isEmpty()) {
            return List.of();
        }

        List<String> alumnoIds = solicitudes.stream()
                .map(SolicitudesGrupos::getAlumnoId)
                .distinct()
                .toList();

        Map<String, Alumno> alumnosPorId = alumnoRepository.findAllById(alumnoIds).stream()
                .collect(Collectors.toMap(Alumno::getId, Function.identity()));

        return solicitudes.stream()
                .filter(s -> alumnosPorId.containsKey(s.getAlumnoId())
                        && gruposPorId.containsKey(s.getGrupoId()))
                .map(s -> MapperSolicitudesGrupos.toDTO(
                        s,
                        alumnosPorId.get(s.getAlumnoId()),
                        gruposPorId.get(s.getGrupoId())))
                .toList();
    }

    private SolicitudesGrupos cargarSolicitudDelDocente(
            String solicitudId, String docenteId, String mensajeSinPermiso) {

        if (solicitudId == null || solicitudId.isBlank()) {
            throw new IllegalArgumentException("El id de la solicitud es obligatorio");
        }
        if (docenteId == null || docenteId.isBlank()) {
            throw new IllegalArgumentException("El id del docente es obligatorio");
        }

        SolicitudesGrupos solicitud = solicitudesRepository.findById(solicitudId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró una solicitud con este id: " + solicitudId));

        Grupo grupo = grupoRepository.findById(solicitud.getGrupoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró un grupo con este id: " + solicitud.getGrupoId()));

        if (grupo.getDocente() == null || !grupo.getDocente().getId().equals(docenteId)) {
            throw new IllegalArgumentException(mensajeSinPermiso);
        }

        return solicitud;
    }
}