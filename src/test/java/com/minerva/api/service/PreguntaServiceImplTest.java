package com.minerva.api.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minerva.api.model.Opcion;
import com.minerva.api.model.Pregunta;
import com.minerva.api.model.QuizXPregunta;
import com.minerva.api.repository.ConceptoRepository;
import com.minerva.api.repository.OpcionRepository;
import com.minerva.api.repository.PreguntaRepository;
import com.minerva.api.repository.QuizXPreguntaRepository;

@ExtendWith(MockitoExtension.class)
class PreguntaServiceImplTest {

    @Mock
    private PreguntaRepository preguntaRepository;

    @Mock
    private ConceptoRepository conceptoRepository;

    @Mock
    private OpcionRepository opcionRepository;

    @Mock
    private QuizXPreguntaRepository quizXPreguntaRepository;

    @InjectMocks
    private PreguntaServiceImpl preguntaService;

    @Test
    void deletePregunta_shouldDeleteRelatedOptionsAndQuizAssignments() {
        Pregunta pregunta = new Pregunta();
        pregunta.setId("P-1");

        Opcion opcion = new Opcion();
        opcion.setId("O-1");
        opcion.setPregunta(pregunta);

        QuizXPregunta quizXPregunta = new QuizXPregunta();
        quizXPregunta.setId("QXP-1");
        quizXPregunta.setPregunta(pregunta);

        when(preguntaRepository.findById("P-1")).thenReturn(Optional.of(pregunta));
        when(opcionRepository.findByPreguntaId("P-1")).thenReturn(List.of(opcion));
        when(quizXPreguntaRepository.findByPreguntaId("P-1")).thenReturn(List.of(quizXPregunta));

        preguntaService.deletePregunta("P-1");

        verify(opcionRepository).deleteAll(List.of(opcion));
        verify(quizXPreguntaRepository).deleteAll(List.of(quizXPregunta));
        verify(preguntaRepository).delete(pregunta);
    }
}
