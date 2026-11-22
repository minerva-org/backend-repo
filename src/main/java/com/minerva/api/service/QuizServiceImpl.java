package com.minerva.api.service;

import java.time.Instant;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.QuizDTO;
import com.minerva.api.User.Roles;
import com.minerva.api.User.User;
import com.minerva.api.security.RolAutorizacion;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Quiz;
import com.minerva.api.model.QuizXPregunta;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.PersonaRepository;
import com.minerva.api.repository.QuizRepository;
import com.minerva.api.repository.QuizXPreguntaRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final GrupoRepository grupoRepository;
    private final PersonaRepository personaRepository;
    private final QuizXPreguntaRepository quizXPreguntaRepository;
    private final PreguntaService preguntaService;

    private void validarLecturaGrupo(Grupo grupo, User usuario) {
        if (grupo == null) {
            throw new EntityNotFoundException("Grupo no encontrado.");
        }
        Roles rol = RolAutorizacion.obtenerRol(usuario);

        if (rol == Roles.ADMIN || rol == Roles.DEV || rol == Roles.DIRECTOR_GENERAL) {
            return;
        }
        if (rol == Roles.DIRECTOR_PLANTEL) {
            Long plantelUsuario = usuario != null
                    && usuario.getPersona() != null
                    && usuario.getPersona().getPlantel() != null
                            ? usuario.getPersona().getPlantel().getId()
                            : null;
            if (plantelUsuario == null
                    || grupo.getPlantel() == null
                    || !plantelUsuario.equals(grupo.getPlantel().getId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No tienes permiso para consultar quizzes de este grupo.");
            }
            return;
        }
        if (rol == Roles.DOCENTE || rol == Roles.COORDINADOR) {
            if (grupo.getDocente() == null
                    || usuario == null
                    || usuario.getPersona() == null
                    || !grupo.getDocente().getId().equals(usuario.getPersona().getId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No tienes permiso para consultar quizzes de este grupo.");
            }
            return;
        }
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No tienes permiso para consultar quizzes.");
    }

    private void validarGestionGrupo(Grupo grupo, User usuario) {
        Roles rol = RolAutorizacion.obtenerRol(usuario);
        if (rol == Roles.DEV) {
            return;
        }
        if (rol == Roles.DOCENTE || rol == Roles.COORDINADOR) {
            if (grupo.getDocente() != null
                    && usuario != null
                    && usuario.getPersona() != null
                    && grupo.getDocente().getId().equals(usuario.getPersona().getId())) {
                return;
            }
        }
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Solo el docente asignado puede crear o modificar quizzes.");
    }

    @Override
    @Transactional
    public QuizDTO saveQuiz(QuizDTO quizDTO) {
        if (quizDTO.getId() == null || quizDTO.getId().isBlank()) {
            throw new IllegalArgumentException("El id es obligatorio");
        }
        String id = quizDTO.getId().trim();
        if (quizRepository.existsById(id)) {
            throw new IllegalArgumentException("Ya existe un quiz con ese Id: " + id);
        }

        if (quizDTO.getNombre() == null || quizDTO.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        String nombre = quizDTO.getNombre().trim();

        if (quizDTO.getGrupoId() == null || quizDTO.getGrupoId().isBlank()) {
            throw new IllegalArgumentException("El grupo es obligatorio");
        }
        String grupoId = quizDTO.getGrupoId().trim();
        Grupo grupo = grupoRepository.findById(grupoId)
            .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado con el id: " + grupoId));

        if (quizRepository.existsByNombreIgnoreCaseAndGrupoId(nombre, grupoId)) {
            throw new IllegalArgumentException("Ya existe un quiz con ese nombre en este grupo");
        }

        Instant fechaInicio = quizDTO.getFechaInicio();
        Instant fechaFinalizacion = quizDTO.getFechaFinalizacion();
        validarFechas(fechaInicio, fechaFinalizacion);

        Quiz quiz = new Quiz();
        quiz.setId(id);
        quiz.setNombre(nombre);
        quiz.setFechaCreacion(Instant.now());
        quiz.setFechaInicio(fechaInicio);
        quiz.setFechaFinalizacion(fechaFinalizacion);
        quiz.setGrupo(grupo);

        return Mapper.toDTO(quizRepository.save(quiz));
    }

    @Override
    @Transactional
    public QuizDTO saveQuiz(QuizDTO quizDTO, User usuario) {
        if (quizDTO == null || quizDTO.getGrupoId() == null || quizDTO.getGrupoId().isBlank()) {
            throw new IllegalArgumentException("El grupo es obligatorio");
        }
        Grupo grupo = grupoRepository.findById(quizDTO.getGrupoId().trim())
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado con el id: " + quizDTO.getGrupoId()));
        validarGestionGrupo(grupo, usuario);
        return saveQuiz(quizDTO);
    }

    @Override
    @Transactional
    public QuizDTO updateQuiz(String quizId, QuizDTO quizDTO) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));

        Grupo grupoDestino = quiz.getGrupo();
        if (quizDTO.getGrupoId() != null) {
            String grupoId = quizDTO.getGrupoId().trim();
            grupoDestino = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("No existe un grupo con el id: " + grupoId));
        }

        String nombreDestino = quiz.getNombre();
        if (quizDTO.getNombre() != null) {
            nombreDestino = quizDTO.getNombre().trim();
            if (nombreDestino.isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
        }

        if (quizRepository.existsByNombreIgnoreCaseAndGrupoIdAndIdNot(
                nombreDestino, grupoDestino.getId(), quizId)) {
            throw new IllegalArgumentException("Ya existe un quiz con ese nombre en este grupo");
        }

        Instant fechaInicioDestino = quizDTO.getFechaInicio() != null
            ? quizDTO.getFechaInicio() : quiz.getFechaInicio();
        Instant fechaFinalizacionDestino = quizDTO.getFechaFinalizacion() != null
            ? quizDTO.getFechaFinalizacion() : quiz.getFechaFinalizacion();
        validarFechas(fechaInicioDestino, fechaFinalizacionDestino);

        quiz.setNombre(nombreDestino);
        quiz.setGrupo(grupoDestino);
        quiz.setFechaInicio(fechaInicioDestino);
        quiz.setFechaFinalizacion(fechaFinalizacionDestino);

        return Mapper.toDTO(quizRepository.save(quiz));
    }

    @Override
    @Transactional
    public QuizDTO updateQuiz(String quizId, QuizDTO quizDTO, User usuario) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));
        validarGestionGrupo(quiz.getGrupo(), usuario);
        if (quizDTO != null && quizDTO.getGrupoId() != null && !quizDTO.getGrupoId().isBlank()) {
            Grupo grupoDestino = grupoRepository.findById(quizDTO.getGrupoId().trim())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No existe un grupo con el id: " + quizDTO.getGrupoId()));
            validarGestionGrupo(grupoDestino, usuario);
        }
        return updateQuiz(quizId, quizDTO);
    }

    private void validarFechas(Instant fechaInicio, Instant fechaFinalizacion) {
        if (fechaInicio != null && fechaFinalizacion != null && !fechaInicio.isBefore(fechaFinalizacion)) {
            throw new IllegalArgumentException(
                "La fecha de inicio debe ser anterior a la fecha de finalización");
        }
    }

    @Override
    @Transactional
    public void deleteQuiz(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));

        List<QuizXPregunta> relacionesQuiz = quizXPreguntaRepository.findByQuizId(quizId);
        if (!relacionesQuiz.isEmpty()) {
            for (QuizXPregunta relacion : relacionesQuiz) {
                if (relacion.getPregunta() != null && relacion.getPregunta().getId() != null) {
                    preguntaService.deletePregunta(relacion.getPregunta().getId());
                }
            }
            quizXPreguntaRepository.deleteAll(relacionesQuiz);
        }

        quizRepository.delete(quiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(String quizId, User usuario) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));
        validarGestionGrupo(quiz.getGrupo(), usuario);
        deleteQuiz(quizId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO> findAll() {
        return quizRepository.findAll()
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO> findAll(User usuario) {
        Roles rol = RolAutorizacion.obtenerRol(usuario);
        if (rol == Roles.ADMIN || rol == Roles.DEV || rol == Roles.DIRECTOR_GENERAL) {
            return findAll();
        }
        if (usuario == null || usuario.getPersona() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autenticado.");
        }
        if (rol == Roles.ALUMNO) {
            return findAllByAlumnoId(usuario.getPersona().getId());
        }
        if (rol == Roles.DIRECTOR_PLANTEL) {
            Long plantelId = usuario.getPersona().getPlantel() != null
                    ? usuario.getPersona().getPlantel().getId()
                    : null;
            if (plantelId == null) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No tienes un plantel asignado.");
            }
            return quizRepository.findAll().stream()
                    .filter(quiz -> quiz.getGrupo() != null
                            && quiz.getGrupo().getPlantel() != null
                            && plantelId.equals(quiz.getGrupo().getPlantel().getId()))
                    .map(Mapper::toDTO)
                    .toList();
        }
        if (rol == Roles.DOCENTE || rol == Roles.COORDINADOR) {
            return quizRepository.findAll().stream()
                    .filter(quiz -> quiz.getGrupo() != null
                            && quiz.getGrupo().getDocente() != null
                            && quiz.getGrupo().getDocente().getId()
                                    .equals(usuario.getPersona().getId()))
                    .map(Mapper::toDTO)
                    .toList();
        }
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No tienes permiso para consultar quizzes.");
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizById(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));

        return Mapper.toDTO(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizById(String quizId, User usuario) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));
        validarLecturaGrupo(quiz.getGrupo(), usuario);
        return Mapper.toDTO(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO> findAllByGrupoId(String grupoId) {
        if (!grupoRepository.existsById(grupoId)) {
            throw new EntityNotFoundException("Grupo no encontrado con el id: " + grupoId);
        }
        return quizRepository.findByGrupoId(grupoId)
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO> findAllByGrupoId(String grupoId, User usuario) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado con el id: " + grupoId));
        validarLecturaGrupo(grupo, usuario);
        return findAllByGrupoId(grupoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO> findAllByAlumnoId(String alumnoId) {
        Persona alumno = personaRepository.findById(alumnoId)
            .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con el id: " + alumnoId));

        if (alumno.getRol() != Roles.ALUMNO) {
            throw new IllegalArgumentException("La persona indicada no tiene rol de alumno");
        }

        List<String> grupoIds = grupoRepository.findByAlumnos_Id(alumnoId)
            .stream()
            .map(grupo -> grupo.getId())
            .toList();

        if (grupoIds.isEmpty()) {
            return List.of();
        }

        return quizRepository.findByGrupoIdIn(grupoIds)
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }
}