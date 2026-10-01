package com.minerva.api.service;

import java.time.Instant;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.QuizDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Quiz;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.PersonaRepository;
import com.minerva.api.repository.QuizRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final GrupoRepository grupoRepository;
    private final PersonaRepository personaRepository;

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

        quizRepository.delete(quiz);
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
    public QuizDTO getQuizById(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));

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
    public List<QuizDTO> findAllByAlumnoId(String alumnoId) {
        Persona alumno = personaRepository.findById(alumnoId)
            .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con el id: " + alumnoId));

        if (alumno.getRol() != com.minerva.api.User.Roles.ALUMNO) {
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