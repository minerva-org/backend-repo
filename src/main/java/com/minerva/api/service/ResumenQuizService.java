package com.minerva.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.ResumenQuizDTO;
import com.minerva.api.model.Quiz;
import com.minerva.api.model.ResumenQuiz;
import com.minerva.api.repository.IntentoRepository;
import com.minerva.api.repository.PuntajeIntento;
import com.minerva.api.repository.QuizRepository;
import com.minerva.api.repository.QuizXPreguntaRepository;
import com.minerva.api.repository.ResumenQuizRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResumenQuizService {

    private final ResumenQuizRepository resumenRepository;
    private final IntentoRepository intentoRepository;
    private final QuizRepository quizRepository;
    private final QuizXPreguntaRepository quizXPreguntaRepository;

    @Value("${app.quiz.calificacion-minima:60}")
    private double calificacionMinima;

    @Transactional
    public void crearVacio(Quiz quiz) {
        ResumenQuiz r = new ResumenQuiz();
        r.setQuiz(quiz);
        resumenRepository.save(r);
    }

    @Transactional(readOnly = true)
    public ResumenQuizDTO obtener(String quizId) {
        return resumenRepository.findByQuizId(quizId)
                .map(this::toDTO)
                .orElseGet(() -> {
                    if (!quizRepository.existsById(quizId)) {
                        throw new EntityNotFoundException("Quiz no encontrado con el id: " + quizId);
                    }
                    return new ResumenQuizDTO(quizId, 0, 0, 0);
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResumenQuizDTO recalcular(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));

        // Primero se bloquea la fila y después se lee
        ResumenQuiz resumen = resumenRepository.findByQuizIdForUpdate(quizId)
                .orElseGet(() -> {
                    ResumenQuiz nuevo = new ResumenQuiz();
                    nuevo.setQuiz(quiz);
                    return resumenRepository.saveAndFlush(nuevo);
                });

        long totalPreguntas = quizXPreguntaRepository.countByQuizId(quizId);
        List<PuntajeIntento> puntajes = intentoRepository.puntajesPorQuiz(quizId);

        double suma = 0;
        int aprobados = 0;
        for (PuntajeIntento p : puntajes) {
            double calificacion = totalPreguntas == 0 ? 0 : p.getCorrectas() * 100.0 / totalPreguntas;
            suma += calificacion;
            if (calificacion >= calificacionMinima) aprobados++;
        }

        int respondieron = puntajes.size();
        resumen.setNumRespondieron(respondieron);
        resumen.setNumAprobados(aprobados);
        resumen.setPromedio(respondieron == 0 ? 0 : Math.round(suma / respondieron * 100.0) / 100.0);

        return toDTO(resumenRepository.save(resumen));
    }

    private ResumenQuizDTO toDTO(ResumenQuiz r) {
        return new ResumenQuizDTO(r.getQuiz().getId(), r.getNumRespondieron(),
                r.getPromedio(), r.getNumAprobados());
    }
}