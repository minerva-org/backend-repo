package com.minerva.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.minerva.api.dto.ResumenQuizDTO;
import com.minerva.api.excepciones.IntentoFinalizadoEvent;
import com.minerva.api.model.Quiz;
import com.minerva.api.model.ResumenQuiz;
import com.minerva.api.repository.IntentoRepository;
import com.minerva.api.repository.PuntajeIntento;
import com.minerva.api.repository.QuizRepository;
import com.minerva.api.repository.QuizXPreguntaRepository;
import com.minerva.api.repository.ResumenQuizRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResumenQuizServiceTest {

    private static final String QUIZ_ID = "q1";

    @Mock ResumenQuizRepository resumenRepository;
    @Mock IntentoRepository intentoRepository;
    @Mock QuizRepository quizRepository;
    @Mock QuizXPreguntaRepository quizXPreguntaRepository;

    @InjectMocks ResumenQuizService service;

    Quiz quiz;
    ResumenQuiz resumen;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "calificacionMinima", 60.0);

        quiz = new Quiz();
        quiz.setId(QUIZ_ID);
        resumen = new ResumenQuiz();
        resumen.setQuiz(quiz);

        when(quizRepository.findById(QUIZ_ID)).thenReturn(Optional.of(quiz));
        when(resumenRepository.findByQuizIdForUpdate(QUIZ_ID)).thenReturn(Optional.of(resumen));
        when(resumenRepository.save(any(ResumenQuiz.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void recalcular_calculaPromedioYAprobados() {
        // 4 preguntas: 4 correctas = 100, 2 = 50, 1 = 25  ->  promedio 58.33, aprobados 1
        when(quizXPreguntaRepository.countByQuizId(QUIZ_ID)).thenReturn(4L);
        when(intentoRepository.puntajesPorQuiz(QUIZ_ID))
                .thenReturn(List.of(puntaje("i1", 4), puntaje("i2", 2), puntaje("i3", 1)));

        ResumenQuizDTO dto = service.recalcular(QUIZ_ID);

        assertThat(dto.numRespondieron()).isEqualTo(3);
        assertThat(dto.promedio()).isEqualTo(58.33);
        assertThat(dto.numAprobados()).isEqualTo(1);
    }

    @Test
    void recalcular_cuentaComoAprobadoElLimiteExacto() {
        // 3 de 5 = 60 exacto, y el mínimo es 60 -> aprueba
        when(quizXPreguntaRepository.countByQuizId(QUIZ_ID)).thenReturn(5L);
        when(intentoRepository.puntajesPorQuiz(QUIZ_ID)).thenReturn(List.of(puntaje("i1", 3)));

        assertThat(service.recalcular(QUIZ_ID).numAprobados()).isEqualTo(1);
    }

    @Test
    void recalcular_sinIntentosDejaTodoEnCero() {
        when(quizXPreguntaRepository.countByQuizId(QUIZ_ID)).thenReturn(4L);
        when(intentoRepository.puntajesPorQuiz(QUIZ_ID)).thenReturn(List.of());

        ResumenQuizDTO dto = service.recalcular(QUIZ_ID);

        assertThat(dto.numRespondieron()).isZero();
        assertThat(dto.promedio()).isZero();
        assertThat(dto.numAprobados()).isZero();
    }

    @Test
    void recalcular_quizSinPreguntasNoDividePorCero() {
        when(quizXPreguntaRepository.countByQuizId(QUIZ_ID)).thenReturn(0L);
        when(intentoRepository.puntajesPorQuiz(QUIZ_ID)).thenReturn(List.of(puntaje("i1", 0)));

        ResumenQuizDTO dto = service.recalcular(QUIZ_ID);

        assertThat(dto.promedio()).isZero();
        assertThat(dto.numAprobados()).isZero();
    }

    @Test
    void recalcular_esIdempotente() {
        when(quizXPreguntaRepository.countByQuizId(QUIZ_ID)).thenReturn(4L);
        when(intentoRepository.puntajesPorQuiz(QUIZ_ID)).thenReturn(List.of(puntaje("i1", 3)));

        ResumenQuizDTO primero = service.recalcular(QUIZ_ID);
        ResumenQuizDTO segundo = service.recalcular(QUIZ_ID);

        assertThat(segundo).isEqualTo(primero);
    }

    @Test
    void listener_noPropagaErroresDelRecalculo() {
        ResumenQuizService mockService = org.mockito.Mockito.mock(ResumenQuizService.class);
        doThrow(new RuntimeException("falló la base")).when(mockService).recalcular(QUIZ_ID);
        ResumenQuizListener listener = new ResumenQuizListener(mockService);

        // no debe lanzar: el alumno ya finalizó su intento
        listener.alFinalizarIntento(new IntentoFinalizadoEvent(QUIZ_ID));
    }

    private PuntajeIntento puntaje(String intentoId, long correctas) {
        return new PuntajeIntento() {
            public String getIntentoId() { return intentoId; }
            public long getCorrectas() { return correctas; }
        };
    }
}