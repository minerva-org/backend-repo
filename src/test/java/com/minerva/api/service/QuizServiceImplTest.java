package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.minerva.api.User.Roles;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Pregunta;
import com.minerva.api.model.Quiz;
import com.minerva.api.model.Plantel;
import com.minerva.api.User.User;
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

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Roles rol) {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(
                        "usuario-test",
                        null,
                        "ROLE_" + rol.name()));
    }

    @Test
    void findAllByGrupoId_shouldAllowDirectorPlantelFromSameCampus() {
        autenticarComo(Roles.DIRECTOR_PLANTEL);
        Plantel plantel = new Plantel();
        plantel.setId(10L);

        Persona director = new Persona();
        director.setId("DP-1");
        director.setRol(Roles.DIRECTOR_PLANTEL);
        director.setPlantel(plantel);

        User usuario = new User();
        usuario.setPersona(director);

        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        grupo.setPlantel(plantel);

        Quiz quiz = new Quiz();
        quiz.setId("Q-1");
        quiz.setGrupo(grupo);

        when(grupoRepository.findById("G-1")).thenReturn(Optional.of(grupo));
        when(grupoRepository.existsById("G-1")).thenReturn(true);
        when(quizRepository.findByGrupoId("G-1")).thenReturn(List.of(quiz));

        var result = quizService.findAllByGrupoId("G-1", usuario);

        assertEquals(1, result.size());
    }

    @Test
    void saveQuiz_shouldRejectDirectorPlantel() {
        autenticarComo(Roles.DIRECTOR_PLANTEL);
        Plantel plantel = new Plantel();
        plantel.setId(10L);

        Persona director = new Persona();
        director.setId("DP-1");
        director.setRol(Roles.DIRECTOR_PLANTEL);
        director.setPlantel(plantel);

        User usuario = new User();
        usuario.setPersona(director);

        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        grupo.setPlantel(plantel);

        com.minerva.api.dto.QuizDTO dto =
                com.minerva.api.dto.QuizDTO.builder().grupoId("G-1").build();

        when(grupoRepository.findById("G-1")).thenReturn(Optional.of(grupo));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> quizService.saveQuiz(dto, usuario));
    }

    @Test
    void findAllByGrupoId_shouldRejectDirectorPlantelFromOtherCampus() {
        autenticarComo(Roles.DIRECTOR_PLANTEL);

        Plantel plantelDirector = new Plantel();
        plantelDirector.setId(10L);
        Plantel plantelGrupo = new Plantel();
        plantelGrupo.setId(20L);

        Persona director = new Persona();
        director.setId("DP-1");
        director.setRol(Roles.DIRECTOR_PLANTEL);
        director.setPlantel(plantelDirector);

        User usuario = new User();
        usuario.setPersona(director);

        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        grupo.setPlantel(plantelGrupo);

        when(grupoRepository.findById("G-1")).thenReturn(Optional.of(grupo));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> quizService.findAllByGrupoId("G-1", usuario));
    }

    @Test
    void findAllByGrupoId_shouldAllowDirectorGeneral() {
        autenticarComo(Roles.DIRECTOR_GENERAL);

        Plantel plantel = new Plantel();
        plantel.setId(10L);
        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        grupo.setPlantel(plantel);

        Persona persona = new Persona();
        persona.setId("DG-1");
        persona.setRol(Roles.DIRECTOR_GENERAL);
        User usuario = new User();
        usuario.setPersona(persona);

        Quiz quiz = new Quiz();
        quiz.setId("Q-1");
        quiz.setGrupo(grupo);

        when(grupoRepository.findById("G-1")).thenReturn(Optional.of(grupo));
        when(grupoRepository.existsById("G-1")).thenReturn(true);
        when(quizRepository.findByGrupoId("G-1")).thenReturn(List.of(quiz));

        var result = quizService.findAllByGrupoId("G-1", usuario);

        assertEquals(1, result.size());
    }

    @Test
    void saveQuiz_shouldRejectDirectorGeneral() {
        autenticarComo(Roles.DIRECTOR_GENERAL);

        Persona persona = new Persona();
        persona.setId("DG-1");
        persona.setRol(Roles.DIRECTOR_GENERAL);
        User usuario = new User();
        usuario.setPersona(persona);

        Grupo grupo = new Grupo();
        grupo.setId("G-1");

        com.minerva.api.dto.QuizDTO dto =
                com.minerva.api.dto.QuizDTO.builder().grupoId("G-1").build();

        when(grupoRepository.findById("G-1")).thenReturn(Optional.of(grupo));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> quizService.saveQuiz(dto, usuario));
    }

    @Test
    void deleteQuiz_shouldAllowAssignedCoordinator() {
        autenticarComo(Roles.COORDINADOR);

        Persona coordinador = new Persona();
        coordinador.setId("COORD-1");
        coordinador.setRol(Roles.COORDINADOR);
        User usuario = new User();
        usuario.setPersona(coordinador);

        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        grupo.setDocente(coordinador);

        Quiz quiz = new Quiz();
        quiz.setId("Q-1");
        quiz.setGrupo(grupo);

        when(quizRepository.findById("Q-1")).thenReturn(Optional.of(quiz));
        when(quizXPreguntaRepository.findByQuizId("Q-1")).thenReturn(List.of());

        quizService.deleteQuiz("Q-1", usuario);

        verify(quizRepository).delete(quiz);
    }

    @Test
    void deleteQuiz_shouldRejectCoordinatorFromAnotherGroup() {
        autenticarComo(Roles.COORDINADOR);

        Persona coordinador = new Persona();
        coordinador.setId("COORD-1");
        coordinador.setRol(Roles.COORDINADOR);
        User usuario = new User();
        usuario.setPersona(coordinador);

        Persona otroDocente = new Persona();
        otroDocente.setId("DOC-2");

        Grupo grupo = new Grupo();
        grupo.setId("G-1");
        grupo.setDocente(otroDocente);

        Quiz quiz = new Quiz();
        quiz.setId("Q-1");
        quiz.setGrupo(grupo);

        when(quizRepository.findById("Q-1")).thenReturn(Optional.of(quiz));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> quizService.deleteQuiz("Q-1", usuario));
    }

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

        when(personaRepository.findById("A-1")).thenReturn(Optional.of(alumno));
        when(grupoRepository.findByAlumnos_Id("A-1")).thenReturn(List.of(grupo));
        when(quizRepository.findByGrupoIdIn(List.of("G-1"))).thenReturn(List.of(quiz));

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

        when(quizRepository.findById("Q-1")).thenReturn(Optional.of(quiz));
        when(quizXPreguntaRepository.findByQuizId("Q-1")).thenReturn(List.of(relacion));

        quizService.deleteQuiz("Q-1");

        verify(preguntaService).deletePregunta("P-1");
        verify(quizXPreguntaRepository).deleteAll(List.of(relacion));
        verify(quizRepository).delete(quiz);
    }
}