
package com.minerva.api.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.User.User;
import com.minerva.api.dto.EstadisticasGeneralesDTO;
import com.minerva.api.dto.EstadisticasQuizDTO;
import com.minerva.api.dto.EstadisticasGrupoDTO;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Intento;
import com.minerva.api.model.IntentoDetalle;
import com.minerva.api.model.Quiz;
import com.minerva.api.repository.GrupoRepository;
import com.minerva.api.repository.IntentoDetalleRepository;
import com.minerva.api.repository.IntentoRepository;
import com.minerva.api.repository.QuizRepository;
import com.minerva.api.security.RolAutorizacion;

@Service
@Transactional(readOnly = true)
public class EstadisticasServiceImpl implements EstadisticasService {

    private static final double CALIFICACION_APROBATORIA = 70.0;

    private final GrupoRepository grupoRepository;
    private final IntentoRepository intentoRepository;
    private final IntentoDetalleRepository detalleRepository;
    private final QuizRepository quizRepository;

    public EstadisticasServiceImpl(
            GrupoRepository grupoRepository,
            IntentoRepository intentoRepository,
            IntentoDetalleRepository detalleRepository,
            QuizRepository quizRepository) {

        this.grupoRepository = grupoRepository;
        this.intentoRepository = intentoRepository;
        this.detalleRepository = detalleRepository;
        this.quizRepository = quizRepository;
    }

    @Override
    public EstadisticasGeneralesDTO obtenerEstadisticasGenerales(User usuario) {

        List<Grupo> grupos = obtenerGruposPermitidos(usuario);

        if (grupos.isEmpty()) {
            return estadisticasVacias();
        }

        List<String> grupoIds = grupos.stream()
                .map(Grupo::getId)
                .toList();

        List<Intento> intentos = intentoRepository
                .findByQuiz_Grupo_IdInAndFechaEnvioIsNotNull(grupoIds);

        if (intentos.isEmpty()) {
            return estadisticasVacias();
        }

        List<String> intentoIds = intentos.stream()
                .map(Intento::getId)
                .toList();

        List<IntentoDetalle> detalles = detalleRepository
                .findByIntento_IdIn(intentoIds);

        return calcularEstadisticasGenerales(intentos, detalles);
    }

    @Override
    public EstadisticasQuizDTO obtenerEstadisticasQuiz(
            String quizId,
            User usuario) {

        if (usuario == null || usuario.getPersona() == null) {
            throw new AccessDeniedException("Usuario no autenticado.");
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el quiz solicitado."));

        List<Grupo> gruposPermitidos = obtenerGruposPermitidos(usuario);

        boolean tieneAcceso = gruposPermitidos.stream()
                .anyMatch(grupo -> grupo.getId().equals(
                        quiz.getGrupo().getId()));

        if (!tieneAcceso) {
            throw new AccessDeniedException(
                    "No tienes permiso para consultar este quiz.");
        }

        List<Intento> intentos = intentoRepository
                .findByQuizId(quizId)
                .stream()
                .filter(intento -> intento.getFechaEnvio() != null)
                .toList();

        if (intentos.isEmpty()) {
            return estadisticasQuizVacias(quiz);
        }

        List<String> intentoIds = intentos.stream()
                .map(Intento::getId)
                .toList();

        List<IntentoDetalle> detalles = detalleRepository
                .findByIntento_IdIn(intentoIds);

        return calcularEstadisticasQuiz(quiz, intentos, detalles);
    }

    @Override
    public EstadisticasGrupoDTO obtenerEstadisticasGrupo(
            String grupoId,
            User usuario) {

        if (usuario == null || usuario.getPersona() == null) {
            throw new AccessDeniedException("Usuario no autenticado.");
        }

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el grupo solicitado."));

        List<Grupo> gruposPermitidos = obtenerGruposPermitidos(usuario);

        boolean tieneAcceso = gruposPermitidos.stream()
                .anyMatch(grupoPermitido -> grupoPermitido.getId().equals(grupoId));

        if (!tieneAcceso) {
            throw new AccessDeniedException(
                    "No tienes permiso para consultar las estadísticas de este grupo.");
        }

        List<Intento> intentos = intentoRepository
                .findByQuiz_Grupo_IdInAndFechaEnvioIsNotNull(List.of(grupoId));

        if (intentos.isEmpty()) {
            return estadisticasGrupoVacias(grupo);
        }

        List<String> intentoIds = intentos.stream()
                .map(Intento::getId)
                .toList();

        List<IntentoDetalle> detalles = detalleRepository
                .findByIntento_IdIn(intentoIds);

        return calcularEstadisticasGrupo(grupo, intentos, detalles);
    }

    private List<Grupo> obtenerGruposPermitidos(User usuario) {

        if (usuario == null || usuario.getPersona() == null) {
            throw new AccessDeniedException("Usuario no autenticado.");
        }

        var persona = usuario.getPersona();
        var rol = RolAutorizacion.obtenerRol(usuario);

        if (rol == null) {
            throw new AccessDeniedException("No se pudo determinar el rol.");
        }

        return switch (rol) {

            case DOCENTE ->
                grupoRepository.findByDocenteId(persona.getId());

            case COORDINADOR, DIRECTOR_PLANTEL ->
                grupoRepository.findByPlantelId(
                        persona.getPlantel().getId());

            case DEV, DIRECTOR_GENERAL ->
                grupoRepository.findAll();

            default ->
                throw new AccessDeniedException(
                        "No tienes permiso para consultar estadísticas.");
        };
    }

    private EstadisticasGeneralesDTO calcularEstadisticasGenerales(
            List<Intento> intentos,
            List<IntentoDetalle> detalles) {

        long respuestasCorrectas = 0;
        long preguntasTotales = 0;
        long incidenciasFoco = 0;
        long intentosAprobados = 0;
        long intentosReprobados = 0;
        long intentosLimpios = 0;

        Set<String> alumnosEvaluados = new HashSet<>();

        for (Intento intento : intentos) {

            List<IntentoDetalle> detallesIntento = detalles.stream()
                    .filter(detalle -> detalle.getIntento() != null
                            && intento.getId().equals(
                                    detalle.getIntento().getId()))
                    .toList();

            long correctasIntento = 0;
            long preguntasIntento = detallesIntento.size();
            long incidenciasIntento = 0;

            for (IntentoDetalle detalle : detallesIntento) {

                if (Boolean.TRUE.equals(detalle.getEsCorrecto())) {
                    correctasIntento++;
                }

                incidenciasIntento += Math.max(
                        0,
                        detalle.getCambiosPantalla());
            }

            respuestasCorrectas += correctasIntento;
            preguntasTotales += preguntasIntento;
            incidenciasFoco += incidenciasIntento;

            if (incidenciasIntento == 0) {
                intentosLimpios++;
            }

            if (preguntasIntento > 0) {

                double calificacion = (correctasIntento * 100.0) / preguntasIntento;

                if (calificacion >= CALIFICACION_APROBATORIA) {
                    intentosAprobados++;
                } else {
                    intentosReprobados++;
                }
            }

            if (intento.getAlumno() != null) {
                alumnosEvaluados.add(intento.getAlumno().getId());
            }
        }

        double promedioGeneral = preguntasTotales == 0
                ? 0
                : (respuestasCorrectas * 100.0) / preguntasTotales;

        long intentosConCalificacion = intentosAprobados + intentosReprobados;

        double porcentajeAprobacion = intentosConCalificacion == 0
                ? 0
                : (intentosAprobados * 100.0) / intentosConCalificacion;

        double porcentajeReprobacion = intentosConCalificacion == 0
                ? 0
                : (intentosReprobados * 100.0) / intentosConCalificacion;

        double indiceFocoLimpio = (intentosLimpios * 100.0) / intentos.size();

        return new EstadisticasGeneralesDTO(
                redondear(promedioGeneral),
                redondear(porcentajeAprobacion),
                redondear(porcentajeReprobacion),
                redondear(indiceFocoLimpio),
                alumnosEvaluados.size(),
                intentos.size(),
                incidenciasFoco,
                intentosAprobados,
                intentosReprobados);
    }

    private EstadisticasQuizDTO calcularEstadisticasQuiz(
            Quiz quiz,
            List<Intento> intentos,
            List<IntentoDetalle> detalles) {

        long respuestasCorrectas = 0;
        long preguntasTotales = 0;
        long incidenciasFoco = 0;
        long intentosAprobados = 0;
        long intentosReprobados = 0;
        long intentosLimpios = 0;

        Set<String> alumnosEvaluados = new HashSet<>();

        for (Intento intento : intentos) {

            List<IntentoDetalle> detallesIntento = detalles.stream()
                    .filter(detalle -> detalle.getIntento() != null
                            && intento.getId().equals(
                                    detalle.getIntento().getId()))
                    .toList();

            long correctasIntento = 0;
            long preguntasIntento = detallesIntento.size();
            long incidenciasIntento = 0;

            for (IntentoDetalle detalle : detallesIntento) {

                if (Boolean.TRUE.equals(detalle.getEsCorrecto())) {
                    correctasIntento++;
                }

                incidenciasIntento += Math.max(
                        0,
                        detalle.getCambiosPantalla());
            }

            respuestasCorrectas += correctasIntento;
            preguntasTotales += preguntasIntento;
            incidenciasFoco += incidenciasIntento;

            if (incidenciasIntento == 0) {
                intentosLimpios++;
            }

            if (preguntasIntento > 0) {

                double calificacion = (correctasIntento * 100.0) / preguntasIntento;

                if (calificacion >= CALIFICACION_APROBATORIA) {
                    intentosAprobados++;
                } else {
                    intentosReprobados++;
                }
            }

            if (intento.getAlumno() != null) {
                alumnosEvaluados.add(intento.getAlumno().getId());
            }
        }

        double promedioGeneral = preguntasTotales == 0
                ? 0
                : (respuestasCorrectas * 100.0) / preguntasTotales;

        long intentosConCalificacion = intentosAprobados + intentosReprobados;

        double porcentajeAprobacion = intentosConCalificacion == 0
                ? 0
                : (intentosAprobados * 100.0) / intentosConCalificacion;

        double porcentajeReprobacion = intentosConCalificacion == 0
                ? 0
                : (intentosReprobados * 100.0) / intentosConCalificacion;

        double indiceFocoLimpio = (intentosLimpios * 100.0) / intentos.size();

        return new EstadisticasQuizDTO(
                quiz.getId(),
                quiz.getNombre(),
                redondear(promedioGeneral),
                redondear(porcentajeAprobacion),
                redondear(porcentajeReprobacion),
                redondear(indiceFocoLimpio),
                alumnosEvaluados.size(),
                intentos.size(),
                incidenciasFoco,
                intentosAprobados,
                intentosReprobados);
    }

    private EstadisticasGeneralesDTO estadisticasVacias() {
        return new EstadisticasGeneralesDTO(
                0, 0, 0, 0,
                0, 0, 0, 0, 0);
    }

    private EstadisticasQuizDTO estadisticasQuizVacias(Quiz quiz) {
        return new EstadisticasQuizDTO(
                quiz.getId(),
                quiz.getNombre(),
                0, 0, 0, 0,
                0, 0, 0, 0, 0);
    }

    private EstadisticasGrupoDTO calcularEstadisticasGrupo(
            Grupo grupo,
            List<Intento> intentos,
            List<IntentoDetalle> detalles) {

        long respuestasCorrectas = 0;
        long preguntasTotales = 0;
        long incidenciasFoco = 0;
        long intentosAprobados = 0;
        long intentosReprobados = 0;
        long intentosLimpios = 0;

        Set<String> alumnosEvaluados = new HashSet<>();
        Set<String> quizzesAplicados = new HashSet<>();

        for (Intento intento : intentos) {

            if (intento.getQuiz() != null) {
                quizzesAplicados.add(intento.getQuiz().getId());
            }

            List<IntentoDetalle> detallesIntento = detalles.stream()
                    .filter(detalle -> detalle.getIntento() != null
                            && intento.getId().equals(
                                    detalle.getIntento().getId()))
                    .toList();

            long correctasIntento = 0;
            long preguntasIntento = detallesIntento.size();
            long incidenciasIntento = 0;

            for (IntentoDetalle detalle : detallesIntento) {

                if (Boolean.TRUE.equals(detalle.getEsCorrecto())) {
                    correctasIntento++;
                }

                incidenciasIntento += Math.max(
                        0,
                        detalle.getCambiosPantalla());
            }

            respuestasCorrectas += correctasIntento;
            preguntasTotales += preguntasIntento;
            incidenciasFoco += incidenciasIntento;

            if (incidenciasIntento == 0) {
                intentosLimpios++;
            }

            if (preguntasIntento > 0) {

                double calificacion = (correctasIntento * 100.0) / preguntasIntento;

                if (calificacion >= CALIFICACION_APROBATORIA) {
                    intentosAprobados++;
                } else {
                    intentosReprobados++;
                }
            }

            if (intento.getAlumno() != null) {
                alumnosEvaluados.add(intento.getAlumno().getId());
            }
        }

        double promedioGeneral = preguntasTotales == 0
                ? 0
                : (respuestasCorrectas * 100.0) / preguntasTotales;

        long intentosConCalificacion = intentosAprobados + intentosReprobados;

        double porcentajeAprobacion = intentosConCalificacion == 0
                ? 0
                : (intentosAprobados * 100.0) / intentosConCalificacion;

        double porcentajeReprobacion = intentosConCalificacion == 0
                ? 0
                : (intentosReprobados * 100.0) / intentosConCalificacion;

        double indiceFocoLimpio = (intentosLimpios * 100.0) / intentos.size();

        return new EstadisticasGrupoDTO(
                grupo.getId(),
                grupo.getNombre(),
                grupo.getClaveGrupo(),
                grupo.getSemestre(),
                redondear(promedioGeneral),
                redondear(porcentajeAprobacion),
                redondear(porcentajeReprobacion),
                redondear(indiceFocoLimpio),
                alumnosEvaluados.size(),
                quizzesAplicados.size(),
                intentos.size(),
                incidenciasFoco,
                intentosAprobados,
                intentosReprobados);
    }

    private EstadisticasGrupoDTO estadisticasGrupoVacias(Grupo grupo) {

        return new EstadisticasGrupoDTO(
                grupo.getId(),
                grupo.getNombre(),
                grupo.getClaveGrupo(),
                grupo.getSemestre(),
                0, 0, 0, 0,
                0, 0, 0, 0, 0, 0);
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

}
