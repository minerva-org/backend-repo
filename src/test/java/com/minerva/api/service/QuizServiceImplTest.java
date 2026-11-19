package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minerva.api.User.Roles;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Pregunta;
import com.minerva.api.model.Quiz;
import com.minerva.api.model.QuizXPregunta;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.PersonaRepository;
import com.minerva.api.repository.QuizRepository;
import com.minerva.api.repository.QuizXPreguntaRepository;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private QuizXPreguntaRepository quizXPreguntaRepository;

    @Mock
    private PreguntaService preguntaService;

    @InjectMocks
    private QuizServiceImpl quizService;

    @Test
    void findAllByAlumnoId_shouldReturnQuizzesFromAlumnoGroups() {
        Persona alumno = new Persona();
        alumno.setId("A-1");
        alumno.setRol(Roles.ALUMNO);

        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        grupo.addAlumno(alumno);

        Quiz quiz = new Quiz();
        quiz.setId("Q-1");
        quiz.setNombre("Quiz 1");
        quiz.setFechaCreacion(Instant.parse("2026-01-01T00:00:00Z"));
        quiz.setFechaInicio(Instant.parse("2026-01-02T00:00:00Z"));
        quiz.setFechaFinalizacion(Instant.parse("2026-01-03T00:00:00Z"));
        quiz.setGrupo(grupo);

        when(personaRepository.findById("A-1")).thenReturn(java.util.Optional.of(alumno));
        when(grupoRepository.findByAlumnos_Id("A-1")).thenReturn(java.util.List.of(grupo));
        when(quizRepository.findByGrupoIdIn(java.util.List.of("G-1"))).thenReturn(java.util.List.of(quiz));

        var result = quizService.findAllByAlumnoId("A-1");

        assertEquals(1, result.size());
        assertEquals("Q-1", result.get(0).getId());
        assertTrue(result.get(0).getGrupoId().equals("G-1"));
    }

    @Test
    void deleteQuiz_shouldDeleteRelatedQuestionMappings() {
        Quiz quiz = new Quiz();
        quiz.setId("Q-1");
        quiz.setNombre("Quiz 1");
        quiz.setGrupo(new Grupo());
        quiz.getGrupo().setId("G-1");

        QuizXPregunta relacion = new QuizXPregunta();
        relacion.setId("R-1");
        relacion.setQuiz(quiz);
        Pregunta pregunta = new Pregunta();
        pregunta.setId("P-1");
        relacion.setPregunta(pregunta);

        when(quizRepository.findById("Q-1")).thenReturn(java.util.Optional.of(quiz));
        when(quizXPreguntaRepository.findByQuizId("Q-1")).thenReturn(java.util.List.of(relacion));

        quizService.deleteQuiz("Q-1");

        verify(preguntaService).deletePregunta("P-1");
        verify(quizXPreguntaRepository).deleteAll(java.util.List.of(relacion));
        verify(quizRepository).delete(quiz);
    }
}