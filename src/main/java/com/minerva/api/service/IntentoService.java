package com.minerva.api.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.IntentoResponse;
import com.minerva.api.dto.OpcionAlumnoDTO;
import com.minerva.api.dto.PreguntaAlumnoDTO;
import com.minerva.api.dto.RespuestaRequestDTO;
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

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IntentoService {

    private final IntentoRepository intentoRepository;
    private final IntentoDetalleRepository detalleRepository;
    private final QuizRepository quizRepository;
    private final GrupoRepository grupoRepository;
    private final AlumnoRepository alumnoRepository;
    private final QuizXPreguntaRepository quizXPreguntaRepository;
    private final OpcionRepository opcionRepository;

    public record Resultado(long correctas, long enBlanco, long total) {}

    @Transactional
    public IntentoResponse iniciarOReanudar(String quizId, String alumnoId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));

        if (!grupoRepository.existsByIdAndAlumnos_Id(quiz.getGrupo().getId(), alumnoId)) {
            throw new AccessDeniedException("No perteneces al grupo de este quiz");
        }

        Optional<Intento> existente = intentoRepository.findByQuizIdAndAlumnoId(quizId, alumnoId);
        Intento intento;

        if (existente.isPresent()) {
            intento = existente.get();
            if (intento.getFechaEnvio() == null && vencido(quiz)) {
                cerrar(intento);
            }
        } else {
            Instant ahora = Instant.now();
            if (ahora.isBefore(quiz.getFechaInicio())) {
                throw new IllegalStateException("El quiz aún no está disponible");
            }
            if (ahora.isAfter(quiz.getFechaFinalizacion())) {
                throw new IllegalStateException("El quiz ya cerró");
            }
            if (quizXPreguntaRepository.countByQuizId(quizId) == 0) {
                throw new IllegalStateException("El quiz no tiene preguntas");
            }

            intento = new Intento();
            intento.setQuiz(quiz);
            intento.setAlumno(alumnoRepository.getReferenceById(alumnoId));
            intento = intentoRepository.save(intento);
        }

        return estado(intento);
    }

    @Transactional
    public IntentoResponse responder(String quizId, String intentoId, String alumnoId,
                                    RespuestaRequestDTO body) {
        Intento intento = cargarIntento(quizId, intentoId, alumnoId);
        Quiz quiz = intento.getQuiz();

        if (intento.getFechaEnvio() != null) {
            throw new IllegalStateException("El intento ya fue finalizado");
        }
        if (vencido(quiz)) {
            cerrar(intento);
            return estado(intento);
        }

        int respondidas = (int) detalleRepository.countByIntentoId(intentoId);
        int total = (int) quizXPreguntaRepository.countByQuizId(quizId);
        if (respondidas >= total) {
            throw new IllegalStateException("Ya respondiste todas las preguntas; finaliza el intento");
        }

        Pregunta actual = preguntaEnPosicion(quizId, respondidas);
        if (!actual.getId().equals(body.getPreguntaId())) {
            throw new IllegalArgumentException("Esa no es la pregunta actual");
        }

        Opcion opcion = null;
        String opcionId = body.getOpcionId();
        if (opcionId != null && !opcionId.isBlank()) {
            opcion = opcionRepository.findByIdAndPreguntaId(opcionId, actual.getId())
                    .orElseThrow(() -> new IllegalArgumentException("La opción no pertenece a la pregunta"));
        }

        IntentoDetalle detalle = new IntentoDetalle();
        detalle.setIntento(intento);
        detalle.setPregunta(actual);
        detalle.setOpcionSeleccionada(opcion);
        detalle.setCambiosPantalla(body.getCambiosPantalla());
        detalle.setEsCorrecto(opcion != null && Boolean.TRUE.equals(opcion.getEsCorrecta()));
        detalleRepository.save(detalle);

        return construirRespuesta(intento, respondidas + 1, total);
    }

    @Transactional
    public Resultado finalizar(String quizId, String intentoId, String alumnoId) {
        Intento intento = cargarIntento(quizId, intentoId, alumnoId);
        if (intento.getFechaEnvio() == null) {
            cerrar(intento);
        }

        return new Resultado(
                detalleRepository.countByIntentoIdAndEsCorrectoTrue(intentoId),
                detalleRepository.countByIntentoIdAndOpcionSeleccionadaIsNull(intentoId),
                detalleRepository.countByIntentoId(intentoId));
    }

    private Intento cargarIntento(String quizId, String intentoId, String alumnoId) {
        Intento intento = intentoRepository.findById(intentoId)
                .orElseThrow(() -> new EntityNotFoundException("Intento no encontrado con el id: " + intentoId));
        if (!intento.getQuiz().getId().equals(quizId)) {
            throw new EntityNotFoundException("El intento no pertenece a este quiz");
        }
        if (!intento.getAlumno().getId().equals(alumnoId)) {
            throw new AccessDeniedException("Este intento no te pertenece");
        }
        return intento;
    }

    private boolean vencido(Quiz quiz) {
        return Instant.now().isAfter(quiz.getFechaFinalizacion());
    }

    private void cerrar(Intento intento) {
        Set<String> respondidas = detalleRepository.findByIntentoId(intento.getId()).stream()
                .map(d -> d.getPregunta().getId())
                .collect(Collectors.toSet());

        List<QuizXPregunta> preguntas =
                quizXPreguntaRepository.findByQuizIdOrderByIdAsc(intento.getQuiz().getId());

        for (QuizXPregunta qp : preguntas) {
            if (!respondidas.contains(qp.getPregunta().getId())) {
                IntentoDetalle enBlanco = new IntentoDetalle();
                enBlanco.setIntento(intento);
                enBlanco.setPregunta(qp.getPregunta());
                enBlanco.setEsCorrecto(false);
                detalleRepository.save(enBlanco);
            }
        }

        Instant limite = intento.getQuiz().getFechaFinalizacion();
        Instant ahora = Instant.now();
        intento.setFechaEnvio(ahora.isAfter(limite) ? limite : ahora);
        intentoRepository.save(intento);
    }

    private Pregunta preguntaEnPosicion(String quizId, int posicion) {
        List<QuizXPregunta> fila = quizXPreguntaRepository
                .findByQuizIdOrderByIdAsc(quizId, PageRequest.of(posicion, 1));
        if (fila.isEmpty()) {
            throw new IllegalStateException("No se encontró la pregunta en la posición " + (posicion + 1));
        }
        return fila.get(0).getPregunta();
    }

    private IntentoResponse estado(Intento intento) {
        String quizId = intento.getQuiz().getId();
        int respondidas = (int) detalleRepository.countByIntentoId(intento.getId());
        int total = (int) quizXPreguntaRepository.countByQuizId(quizId);
        return construirRespuesta(intento, respondidas, total);
    }

    private IntentoResponse construirRespuesta(Intento intento, int respondidas, int total) {
        Quiz quiz = intento.getQuiz();
        boolean finalizado = intento.getFechaEnvio() != null;
        PreguntaAlumnoDTO actual = null;

        if (!finalizado && respondidas < total) {
            Pregunta p = preguntaEnPosicion(quiz.getId(), respondidas);
            List<OpcionAlumnoDTO> opciones = opcionRepository.findByPreguntaIdOrderByIdAsc(p.getId())
                    .stream()
                    .map(o -> new OpcionAlumnoDTO(o.getId(), o.getDescripcion()))
                    .toList();
            actual = new PreguntaAlumnoDTO(p.getId(), p.getDescripcion(), respondidas + 1, total, opciones);
        }

        return new IntentoResponse(intento.getId(), quiz.getId(), quiz.getFechaFinalizacion(),
                finalizado, actual);
    }
}