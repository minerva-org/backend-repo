package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.dto.QuizXPreguntaDTO;
import com.minerva.api.model.Concepto;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Pregunta;
import com.minerva.api.model.Quiz;
import com.minerva.api.model.QuizXPregunta;
import com.minerva.api.repository.ConceptoGrupoRepository;
import com.minerva.api.repository.PreguntaRepository;
import com.minerva.api.repository.QuizRepository;
import com.minerva.api.repository.QuizXPreguntaRepository;

@ExtendWith(MockitoExtension.class)
class QuizXPreguntaServiceImplTest {

    @Mock private QuizXPreguntaRepository quizXPreguntaRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private PreguntaRepository preguntaRepository;
    @Mock private ConceptoGrupoRepository conceptoGrupoRepository;

    @InjectMocks
    private QuizXPreguntaServiceImpl service;

    // Input: quiz G-1 y pregunta con concepto perteneciente al currículo G-1.
    // Resultado esperado: crea la relación correctamente.
    @Test
    void save_shouldAllowQuestionFromGroupCurriculum() {
        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        Quiz quiz = new Quiz();
        quiz.setId("Q-1");
        quiz.setGrupo(grupo);
        Concepto concepto = new Concepto();
        concepto.setId("CON-1");
        Pregunta pregunta = new Pregunta();
        pregunta.setId("P-1");
        pregunta.setConcepto(concepto);

        when(quizRepository.findById("Q-1")).thenReturn(Optional.of(quiz));
        when(preguntaRepository.findById("P-1")).thenReturn(Optional.of(pregunta));
        when(conceptoGrupoRepository
                .existsByConceptoOrigenIdAndTemaGrupoUnidadGrupoGrupoId("CON-1", "G-1"))
                .thenReturn(true);
        when(quizXPreguntaRepository.existsById("QXP-1")).thenReturn(false);
        when(quizXPreguntaRepository.save(any(QuizXPregunta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var dto = QuizXPreguntaDTO.builder()
                .id("QXP-1")
                .quizId("Q-1")
                .preguntaId("P-1")
                .build();

        var result = service.saveQuizXPregunta(dto);

        assertEquals("QXP-1", result.getId());
        verify(quizXPreguntaRepository).save(any(QuizXPregunta.class));
    }

    // Input: pregunta cuyo concepto no pertenece al currículo G-1.
    // Resultado esperado: 403 y no guarda relación.
    @Test
    void save_shouldRejectQuestionOutsideGroupCurriculum() {
        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        Quiz quiz = new Quiz();
        quiz.setId("Q-1");
        quiz.setGrupo(grupo);
        Concepto concepto = new Concepto();
        concepto.setId("CON-9");
        Pregunta pregunta = new Pregunta();
        pregunta.setId("P-9");
        pregunta.setConcepto(concepto);

        when(quizRepository.findById("Q-1")).thenReturn(Optional.of(quiz));
        when(preguntaRepository.findById("P-9")).thenReturn(Optional.of(pregunta));
        when(conceptoGrupoRepository
                .existsByConceptoOrigenIdAndTemaGrupoUnidadGrupoGrupoId("CON-9", "G-1"))
                .thenReturn(false);

        var dto = QuizXPreguntaDTO.builder()
                .id("QXP-9")
                .quizId("Q-1")
                .preguntaId("P-9")
                .build();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.saveQuizXPregunta(dto));

        assertEquals(403, error.getStatusCode().value());
        assertTrue(error.getReason().contains("currículo"));
        verify(quizXPreguntaRepository, never()).save(any());
    }

    // Input: pregunta null o concepto null.
    // Resultado esperado: 400 antes de guardar.
    @Test
    void save_shouldRejectNullQuestionConcept() {
        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        Quiz quiz = new Quiz();
        quiz.setId("Q-1");
        quiz.setGrupo(grupo);
        Pregunta pregunta = new Pregunta();
        pregunta.setId("P-1");
        pregunta.setConcepto(null);

        when(quizRepository.findById("Q-1")).thenReturn(Optional.of(quiz));
        when(preguntaRepository.findById("P-1")).thenReturn(Optional.of(pregunta));

        var dto = QuizXPreguntaDTO.builder()
                .id("QXP-1")
                .quizId("Q-1")
                .preguntaId("P-1")
                .build();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.saveQuizXPregunta(dto));

        assertTrue(error.getMessage().contains("concepto"));
        verify(quizXPreguntaRepository, never()).save(any());
    }

    // Input: quiz null.
    // Resultado esperado: 400 descriptivo.
    @Test
    void save_shouldRejectNullQuiz() {
        var dto = QuizXPreguntaDTO.builder()
                .id("QXP-1")
                .quizId("Q-NULO")
                .preguntaId("P-1")
                .build();

        when(quizRepository.findById("Q-NULO")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> service.saveQuizXPregunta(dto));
        verify(quizXPreguntaRepository, never()).save(any());
    }
}
