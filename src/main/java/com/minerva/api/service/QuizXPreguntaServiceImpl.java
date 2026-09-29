package com.minerva.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.PreguntaDTO;
import com.minerva.api.dto.QuizXPreguntaDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.Concepto;
import com.minerva.api.model.Pregunta;
import com.minerva.api.model.Quiz;
import com.minerva.api.model.QuizXPregunta;
import com.minerva.api.repository.ConceptoRepository;
import com.minerva.api.repository.PreguntaRepository;
import com.minerva.api.repository.QuizRepository;
import com.minerva.api.repository.QuizXPreguntaRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizXPreguntaServiceImpl implements QuizXPreguntaService {

    private final QuizXPreguntaRepository quizXPreguntaRepository;
    private final QuizRepository quizRepository;
    private final PreguntaRepository preguntaRepository;

    @Override
    @Transactional
    public QuizXPreguntaDTO saveQuizXPregunta(QuizXPreguntaDTO quizXPreguntaDTO) {
        if (quizXPreguntaDTO.getId() == null || quizXPreguntaDTO.getId().isBlank()) {
            throw new IllegalArgumentException("El id es obligatorio");
        }
        String id = quizXPreguntaDTO.getId().trim();
        if (quizXPreguntaRepository.existsById(id)) {
            throw new IllegalArgumentException("Ya existe un registro con ese Id: " + id);
        }

        if (quizXPreguntaDTO.getQuizId() == null || quizXPreguntaDTO.getQuizId().isBlank()) {
            throw new IllegalArgumentException("El quiz es obligatorio");
        }
        String quizId = quizXPreguntaDTO.getQuizId().trim();
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));

        if (quizXPreguntaDTO.getPreguntaId() == null || quizXPreguntaDTO.getPreguntaId().isBlank()) {
            throw new IllegalArgumentException("La pregunta es obligatoria");
        }
        String preguntaId = quizXPreguntaDTO.getPreguntaId().trim();
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta no encontrada con el id: " + preguntaId));

        QuizXPregunta quizXPregunta = new QuizXPregunta();
        quizXPregunta.setId(id);
        quizXPregunta.setQuiz(quiz);
        quizXPregunta.setPregunta(pregunta);

        return Mapper.toDTO(quizXPreguntaRepository.save(quizXPregunta));
    }

    @Override
    @Transactional
    public QuizXPreguntaDTO updateQuizXPregunta(String id, QuizXPreguntaDTO quizXPreguntaDTO) {
        QuizXPregunta quizXPregunta = quizXPreguntaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Registro no encontrado con el id: " + id));

        String quizIdDestino = quizXPregunta.getQuiz().getId();
        if (quizXPreguntaDTO.getQuizId() != null) {
            String quizId = quizXPreguntaDTO.getQuizId().trim();
            Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));
            quizXPregunta.setQuiz(quiz);
            quizIdDestino = quizId;
        }

        String preguntaIdDestino = quizXPregunta.getPregunta().getId();
        if (quizXPreguntaDTO.getPreguntaId() != null) {
            String preguntaId = quizXPreguntaDTO.getPreguntaId().trim();
            Pregunta pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> new EntityNotFoundException("Pregunta no encontrada con el id: " + preguntaId));
            quizXPregunta.setPregunta(pregunta);
            preguntaIdDestino = preguntaId;
        }


        if (quizXPreguntaRepository.existsByQuizIdAndPreguntaIdAndIdNot(
                quizIdDestino, preguntaIdDestino, id)) {
            throw new IllegalArgumentException("Esta pregunta ya está agregada a este quiz");
        }

        return Mapper.toDTO(quizXPreguntaRepository.save(quizXPregunta));
    }

    @Override
    @Transactional
    public void deleteQuizXPregunta(String id) {
        QuizXPregunta quizXPregunta = quizXPreguntaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Registro no encontrado con el id: " + id));

        quizXPreguntaRepository.delete(quizXPregunta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizXPreguntaDTO> findAll() {
        return quizXPreguntaRepository.findAll()
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizXPreguntaDTO getQuizXPreguntaById(String id) {
        QuizXPregunta quizXPregunta = quizXPreguntaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Registro no encontrado con el id: " + id));

        return Mapper.toDTO(quizXPregunta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PreguntaDTO> findPreguntasByQuizId(String quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new EntityNotFoundException("Quiz no encontrado con el id: " + quizId);
        }
        return quizXPreguntaRepository.findByQuizId(quizId)
            .stream()
            .map(qxp -> Mapper.toDTO(qxp.getPregunta()))
            .toList();
    }
}