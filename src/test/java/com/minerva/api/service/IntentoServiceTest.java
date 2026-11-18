package com.minerva.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import com.minerva.api.dto.IntentoResponse;
import com.minerva.api.dto.OpcionAlumnoDTO;
import com.minerva.api.dto.RespuestaRequestDTO;
import com.minerva.api.excepciones.IntentoFinalizadoEvent;
import com.minerva.api.model.Alumno;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Intento;
import com.minerva.api.model.IntentoDetalle;
import com.minerva.api.model.Opcion;
import com.minerva.api.model.Pregunta;
import com.minerva.api.model.Quiz;
import com.minerva.api.model.QuizXPregunta;
import com.minerva.api.repository.AlumnoRepository;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.IntentoDetalleRepository;
import com.minerva.api.repository.IntentoRepository;
import com.minerva.api.repository.OpcionRepository;
import com.minerva.api.repository.QuizRepository;
import com.minerva.api.repository.QuizXPreguntaRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IntentoServiceTest {

    private static final String QUIZ_ID = "q1";
    private static final String ALUMNO_ID = "a1";
    private static final String INTENTO_ID = "i1";

    @Mock IntentoRepository intentoRepository;
    @Mock IntentoDetalleRepository detalleRepository;
    @Mock QuizRepository quizRepository;
    @Mock GrupoRepository grupoRepository;
    @Mock AlumnoRepository alumnoRepository;
    @Mock QuizXPreguntaRepository quizXPreguntaRepository;
    @Mock OpcionRepository opcionRepository;
    @Mock ApplicationEventPublisher eventos;

    @InjectMocks IntentoService service;

    Quiz quiz;
    Alumno alumno;
    Intento intento;
    Pregunta p1, p2;
    Opcion correcta, incorrecta;

    @BeforeEach
    void setUp() {
        Grupo grupo = new Grupo();
        grupo.setId("g1");

        quiz = new Quiz();
        quiz.setId(QUIZ_ID);
        quiz.setGrupo(grupo);
        quiz.setFechaInicio(Instant.now().minus(1, ChronoUnit.HOURS));
        quiz.setFechaFinalizacion(Instant.now().plus(1, ChronoUnit.HOURS));

        alumno = new Alumno();
        alumno.setId(ALUMNO_ID);

        intento = new Intento();
        intento.setId(INTENTO_ID);
        intento.setQuiz(quiz);
        intento.setAlumno(alumno);

        p1 = pregunta("p1");
        p2 = pregunta("p2");
        correcta = opcion("o1", p1, true);
        incorrecta = opcion("o2", p1, false);

        when(quizRepository.findById(QUIZ_ID)).thenReturn(Optional.of(quiz));
        when(grupoRepository.existsByIdAndAlumnos_Id("g1", ALUMNO_ID)).thenReturn(true);
        when(intentoRepository.findById(INTENTO_ID)).thenReturn(Optional.of(intento));
        when(quizXPreguntaRepository.countByQuizId(QUIZ_ID)).thenReturn(2L);
        when(detalleRepository.save(any(IntentoDetalle.class))).thenAnswer(i -> i.getArgument(0));
        when(intentoRepository.save(any(Intento.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ---------------- iniciarOReanudar ----------------

    @Test
    void iniciar_creaIntentoNuevoYDevuelveLaPrimeraPregunta() {
        when(intentoRepository.findByQuizIdAndAlumnoId(QUIZ_ID, ALUMNO_ID)).thenReturn(Optional.empty());
        when(alumnoRepository.getReferenceById(ALUMNO_ID)).thenReturn(alumno);
        when(detalleRepository.countByIntentoId(any())).thenReturn(0L);
        preguntaEnPosicion(0, p1);
        when(opcionRepository.findByPreguntaIdOrderByIdAsc("p1")).thenReturn(List.of(correcta, incorrecta));

        IntentoResponse r = service.iniciarOReanudar(QUIZ_ID, ALUMNO_ID);

        verify(intentoRepository).save(any(Intento.class));
        assertThat(r.finalizado()).isFalse();
        assertThat(r.preguntaActual().id()).isEqualTo("p1");
        assertThat(r.preguntaActual().numero()).isEqualTo(1);
        assertThat(r.preguntaActual().opciones()).hasSize(2);
    }

    @Test
    void iniciar_reanudaSinCrearOtroIntento() {
        when(intentoRepository.findByQuizIdAndAlumnoId(QUIZ_ID, ALUMNO_ID)).thenReturn(Optional.of(intento));
        when(detalleRepository.countByIntentoId(INTENTO_ID)).thenReturn(1L);   // ya contestó la 1
        preguntaEnPosicion(1, p2);
        when(opcionRepository.findByPreguntaIdOrderByIdAsc("p2")).thenReturn(List.of());

        IntentoResponse r = service.iniciarOReanudar(QUIZ_ID, ALUMNO_ID);

        verify(intentoRepository, never()).save(any(Intento.class));
        assertThat(r.intentoId()).isEqualTo(INTENTO_ID);
        assertThat(r.preguntaActual().id()).isEqualTo("p2");
        assertThat(r.preguntaActual().numero()).isEqualTo(2);
    }

    @Test
    void iniciar_rechazaAlumnoQueNoPerteneceAlGrupo() {
        when(grupoRepository.existsByIdAndAlumnos_Id("g1", ALUMNO_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.iniciarOReanudar(QUIZ_ID, ALUMNO_ID))
                .isInstanceOf(AccessDeniedException.class);
        verify(intentoRepository, never()).save(any(Intento.class));
    }

    @Test
    void iniciar_rechazaQuizQueTodaviaNoAbre() {
        quiz.setFechaInicio(Instant.now().plus(1, ChronoUnit.HOURS));
        quiz.setFechaFinalizacion(Instant.now().plus(2, ChronoUnit.HOURS));
        when(intentoRepository.findByQuizIdAndAlumnoId(QUIZ_ID, ALUMNO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.iniciarOReanudar(QUIZ_ID, ALUMNO_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("aún no");
    }

    @Test
    void iniciar_rechazaQuizYaCerradoSinIntentoPrevio() {
        quiz.setFechaInicio(Instant.now().minus(2, ChronoUnit.HOURS));
        quiz.setFechaFinalizacion(Instant.now().minus(1, ChronoUnit.HOURS));
        when(intentoRepository.findByQuizIdAndAlumnoId(QUIZ_ID, ALUMNO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.iniciarOReanudar(QUIZ_ID, ALUMNO_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cerró");
    }

    // ---------------- responder ----------------

    @Test
    void responder_opcionCorrecta_guardaDetalleCalificadoYAvanza() {
        when(detalleRepository.countByIntentoId(INTENTO_ID)).thenReturn(0L);
        preguntaEnPosicion(0, p1);
        preguntaEnPosicion(1, p2);
        when(opcionRepository.findByIdAndPreguntaId("o1", "p1")).thenReturn(Optional.of(correcta));
        when(opcionRepository.findByPreguntaIdOrderByIdAsc("p2")).thenReturn(List.of());

        IntentoResponse r = service.responder(QUIZ_ID, INTENTO_ID, ALUMNO_ID, body("p1", "o1", 2));

        ArgumentCaptor<IntentoDetalle> captor = ArgumentCaptor.forClass(IntentoDetalle.class);
        verify(detalleRepository).save(captor.capture());
        IntentoDetalle d = captor.getValue();
        assertThat(d.getEsCorrecto()).isTrue();
        assertThat(d.getOpcionSeleccionada()).isSameAs(correcta);
        assertThat(d.getCambiosPantalla()).isEqualTo(2);
        assertThat(d.getPregunta()).isSameAs(p1);

        assertThat(r.preguntaActual().id()).isEqualTo("p2");
        assertThat(r.preguntaActual().numero()).isEqualTo(2);
    }

    @Test
    void responder_opcionIncorrecta_guardaEsCorrectoFalse() {
        when(detalleRepository.countByIntentoId(INTENTO_ID)).thenReturn(0L);
        preguntaEnPosicion(0, p1);
        preguntaEnPosicion(1, p2);
        when(opcionRepository.findByIdAndPreguntaId("o2", "p1")).thenReturn(Optional.of(incorrecta));
        when(opcionRepository.findByPreguntaIdOrderByIdAsc("p2")).thenReturn(List.of());

        service.responder(QUIZ_ID, INTENTO_ID, ALUMNO_ID, body("p1", "o2", 0));

        ArgumentCaptor<IntentoDetalle> captor = ArgumentCaptor.forClass(IntentoDetalle.class);
        verify(detalleRepository).save(captor.capture());
        assertThat(captor.getValue().getEsCorrecto()).isFalse();
    }

    @Test
    void responder_enBlanco_guardaOpcionNulaYNoCorrecta() {
        when(detalleRepository.countByIntentoId(INTENTO_ID)).thenReturn(0L);
        preguntaEnPosicion(0, p1);
        preguntaEnPosicion(1, p2);
        when(opcionRepository.findByPreguntaIdOrderByIdAsc("p2")).thenReturn(List.of());

        service.responder(QUIZ_ID, INTENTO_ID, ALUMNO_ID, body("p1", null, 0));

        ArgumentCaptor<IntentoDetalle> captor = ArgumentCaptor.forClass(IntentoDetalle.class);
        verify(detalleRepository).save(captor.capture());
        assertThat(captor.getValue().getOpcionSeleccionada()).isNull();
        assertThat(captor.getValue().getEsCorrecto()).isFalse();
    }

    @Test
    void responder_rechazaPreguntaQueNoEsLaActual() {
        when(detalleRepository.countByIntentoId(INTENTO_ID)).thenReturn(0L);
        preguntaEnPosicion(0, p1);

        assertThatThrownBy(() -> service.responder(QUIZ_ID, INTENTO_ID, ALUMNO_ID, body("p2", "o1", 0)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(detalleRepository, never()).save(any(IntentoDetalle.class));
    }

    @Test
    void responder_rechazaOpcionDeOtraPregunta() {
        when(detalleRepository.countByIntentoId(INTENTO_ID)).thenReturn(0L);
        preguntaEnPosicion(0, p1);
        when(opcionRepository.findByIdAndPreguntaId("oX", "p1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.responder(QUIZ_ID, INTENTO_ID, ALUMNO_ID, body("p1", "oX", 0)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(detalleRepository, never()).save(any(IntentoDetalle.class));
    }

    @Test
    void responder_rechazaIntentoDeOtroAlumno() {
        assertThatThrownBy(() -> service.responder(QUIZ_ID, INTENTO_ID, "otro", body("p1", "o1", 0)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void responder_rechazaIntentoYaFinalizado() {
        intento.setFechaEnvio(Instant.now());

        assertThatThrownBy(() -> service.responder(QUIZ_ID, INTENTO_ID, ALUMNO_ID, body("p1", "o1", 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void responder_conQuizVencido_cierraElIntentoSinLanzarExcepcion() {
        quiz.setFechaInicio(Instant.now().minus(2, ChronoUnit.HOURS));
        quiz.setFechaFinalizacion(Instant.now().minus(1, ChronoUnit.HOURS));
        when(detalleRepository.findByIntentoId(INTENTO_ID)).thenReturn(List.of());
        when(quizXPreguntaRepository.findByQuizIdOrderByIdAsc(QUIZ_ID)).thenReturn(List.of(qp(p1), qp(p2)));

        IntentoResponse r = service.responder(QUIZ_ID, INTENTO_ID, ALUMNO_ID, body("p1", "o1", 0));

        assertThat(r.finalizado()).isTrue();
        assertThat(r.preguntaActual()).isNull();
        assertThat(intento.getFechaEnvio()).isEqualTo(quiz.getFechaFinalizacion());   // se cierra al límite, no "ahora"
        verify(detalleRepository, org.mockito.Mockito.times(2)).save(any(IntentoDetalle.class)); // 2 en blanco
    }

    // ---------------- finalizar ----------------

    @Test
    void finalizar_registraEnBlancoLasNoContestadas_publicaEventoYDevuelveResultado() {
        IntentoDetalle yaRespondida = new IntentoDetalle();
        yaRespondida.setPregunta(p1);
        when(detalleRepository.findByIntentoId(INTENTO_ID)).thenReturn(List.of(yaRespondida));
        when(quizXPreguntaRepository.findByQuizIdOrderByIdAsc(QUIZ_ID)).thenReturn(List.of(qp(p1), qp(p2)));
        when(detalleRepository.countByIntentoIdAndEsCorrectoTrue(INTENTO_ID)).thenReturn(1L);
        when(detalleRepository.countByIntentoIdAndOpcionSeleccionadaIsNull(INTENTO_ID)).thenReturn(1L);
        when(detalleRepository.countByIntentoId(INTENTO_ID)).thenReturn(2L);

        IntentoService.Resultado res = service.finalizar(QUIZ_ID, INTENTO_ID, ALUMNO_ID);

        // solo se crea el detalle en blanco de p2
        ArgumentCaptor<IntentoDetalle> captor = ArgumentCaptor.forClass(IntentoDetalle.class);
        verify(detalleRepository).save(captor.capture());
        assertThat(captor.getValue().getPregunta()).isSameAs(p2);
        assertThat(captor.getValue().getOpcionSeleccionada()).isNull();
        assertThat(captor.getValue().getEsCorrecto()).isFalse();

        assertThat(intento.getFechaEnvio()).isNotNull();
        verify(eventos).publishEvent(any(IntentoFinalizadoEvent.class));
        assertThat(res).isEqualTo(new IntentoService.Resultado(1, 1, 2));
    }

    @Test
    void finalizar_esIdempotente_noVuelveACerrarNiPublicarEvento() {
        Instant cierre = Instant.now().minusSeconds(30);
        intento.setFechaEnvio(cierre);

        service.finalizar(QUIZ_ID, INTENTO_ID, ALUMNO_ID);

        assertThat(intento.getFechaEnvio()).isEqualTo(cierre);
        verify(detalleRepository, never()).save(any(IntentoDetalle.class));
        verify(eventos, never()).publishEvent(any(Object.class));
    }

    // ---------------- seguridad del DTO ----------------

    @Test
    void opcionAlumnoDTO_noExponeEsCorrecta() {
        assertThat(OpcionAlumnoDTO.class.getRecordComponents())
                .extracting(c -> c.getName())
                .doesNotContain("esCorrecta", "esCorrecto");
    }

    // ---------------- helpers ----------------

    private void preguntaEnPosicion(int pos, Pregunta p) {
        when(quizXPreguntaRepository.findByQuizIdOrderByIdAsc(QUIZ_ID, PageRequest.of(pos, 1)))
                .thenReturn(List.of(qp(p)));
    }

    private QuizXPregunta qp(Pregunta p) {
        QuizXPregunta qp = new QuizXPregunta();
        qp.setQuiz(quiz);
        qp.setPregunta(p);
        return qp;
    }

    private Pregunta pregunta(String id) {
        Pregunta p = new Pregunta();
        p.setId(id);
        p.setDescripcion("Descripción " + id);
        return p;
    }

    private Opcion opcion(String id, Pregunta p, boolean esCorrecta) {
        Opcion o = new Opcion();
        o.setId(id);
        o.setDescripcion("Opción " + id);
        o.setPregunta(p);
        o.setEsCorrecta(esCorrecta);
        return o;
    }

    private RespuestaRequestDTO body(String preguntaId, String opcionId, int cambios) {
        RespuestaRequestDTO b = new RespuestaRequestDTO();
        b.setPreguntaId(preguntaId);
        b.setOpcionId(opcionId);
        b.setCambiosPantalla(cambios);
        return b;
    }
}