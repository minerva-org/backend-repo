package com.minerva.api.service;

import java.time.Instant;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.User.User;
import com.minerva.api.dto.EstadisticasDTO.*;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Intento;
import com.minerva.api.model.IntentoDetalle;
import com.minerva.api.model.Materia;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Plantel;
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

        List<IntentoDetalle> detalles = detalleRepository
                .findByIntento_IdIn(idsDe(intentos));

        return calcularEstadisticasGenerales(intentos, detalles);
    }

    // Estadisticas por materia

    @Override
    public List<EstadisticasMateriaDTO> obtenerEstadisticasMateria(User usuario) {

        List<Grupo> grupos = obtenerGruposPermitidos(usuario);

        if (grupos.isEmpty()) {
            return List.of();
        }

        List<String> grupoIds = grupos.stream()
                .map(Grupo::getId)
                .toList();

        List<Intento> intentos = intentoRepository
                .findByQuiz_Grupo_IdInAndFechaEnvioIsNotNull(grupoIds);

        if (intentos.isEmpty()) {
            return List.of();
        }

        Map<String, List<IntentoDetalle>> detallesPorIntento = agruparDetalles(
                detalleRepository.findByIntento_IdIn(idsDe(intentos)));

        Map<Materia, List<Intento>> intentosPorMateria = intentos.stream()
                .filter(intento -> intento.getQuiz() != null
                        && intento.getQuiz().getGrupo() != null
                        && intento.getQuiz().getGrupo().getMateria() != null)
                .collect(Collectors.groupingBy(
                        intento -> intento.getQuiz().getGrupo().getMateria()));

        return intentosPorMateria.entrySet().stream()
                .map(entrada -> calcularEstadisticasMateria(
                        entrada.getKey(),
                        entrada.getValue(),
                        detallesPorIntento))
                .sorted(Comparator.comparing(EstadisticasMateriaDTO::nombreMateria))
                .toList();
    }

    // Estadisticas de los docentes

    @Override
    public List<EstadisticasDocentesDTO> obtenerEstadisticasDocentes(User usuario) {

        List<Grupo> grupos = obtenerGruposPermitidos(usuario);

        if (grupos.isEmpty()) {
            return List.of();
        }

        List<String> grupoIds = grupos.stream()
                .map(Grupo::getId)
                .toList();

        List<Intento> intentos = intentoRepository
                .findByQuiz_Grupo_IdInAndFechaEnvioIsNotNull(grupoIds);

        Instant ahora = Instant.now();
        Instant desde = ahora.minus(Duration.ofDays(DIAS_ACTIVIDAD_DOCENTE));

        Map<String, List<Intento>> intentosPorDocente = intentos.stream()
                .filter(intento -> intento.getQuiz() != null
                        && intento.getQuiz().getGrupo() != null
                        && intento.getQuiz().getGrupo().getDocente() != null)
                .filter(intento -> {
                    Instant fin = intento.getQuiz().getFechaFinalizacion();
                    return fin != null && !fin.isBefore(desde) && !fin.isAfter(ahora);
                })
                .collect(Collectors.groupingBy(
                        intento -> intento.getQuiz().getGrupo().getDocente().getId()));

        return intentosPorDocente.values().stream()
                .map(this::calcularEstadisticasDocente)
                .sorted(Comparator.comparing(EstadisticasDocentesDTO::quizzesAplicados).reversed())
                .toList();
    }

    private EstadisticasDocentesDTO calcularEstadisticasDocente(List<Intento> intentos) {

        Persona docente = intentos.get(0).getQuiz().getGrupo().getDocente();

        long quizzesAplicados = intentos.stream()
                .map(intento -> intento.getQuiz().getId())
                .distinct()
                .count();

        return new EstadisticasDocentesDTO(
                docente.getId(),
                docente.getNombre(),
                docente.getApellidoPaterno(),
                docente.getApellidoMaterno(),
                quizzesAplicados,
                intentos.size());
    }

    private static final int QUIZZES_EN_TENDENCIA = 6;
    private static final int DIAS_ACTIVIDAD_DOCENTE = 30;

    @Override
    public List<EstadisticasTendenciaDTO> obtenerTendenciaAprobacion(User usuario) {

        List<Grupo> grupos = obtenerGruposPermitidos(usuario);

        if (grupos.isEmpty()) {
            return List.of();
        }

        List<String> grupoIds = grupos.stream()
                .map(Grupo::getId)
                .toList();

        List<Intento> intentos = intentoRepository
                .findByQuiz_Grupo_IdInAndFechaEnvioIsNotNull(grupoIds);

        if (intentos.isEmpty()) {
            return List.of();
        }

        Map<String, List<IntentoDetalle>> detallesPorIntento = agruparDetalles(
                detalleRepository.findByIntento_IdIn(idsDe(intentos)));

        Instant ahora = Instant.now();

        Map<String, List<Intento>> intentosPorQuiz = intentos.stream()
                .filter(intento -> intento.getQuiz() != null)
                .collect(Collectors.groupingBy(intento -> intento.getQuiz().getId()));

        return intentosPorQuiz.values().stream()
                .filter(lista -> {
                    Instant fin = fechaFin(lista);
                    return fin != null && !fin.isAfter(ahora);
                })
                .sorted(Comparator.comparing(this::fechaFin).reversed())
                .limit(QUIZZES_EN_TENDENCIA)
                .sorted(Comparator.comparing(this::fechaFin))
                .map(lista -> {
                    Quiz quiz = lista.get(0).getQuiz();
                    Metricas m = calcularMetricas(lista, detallesPorIntento);

                    return new EstadisticasTendenciaDTO(
                            quiz.getId(),
                            quiz.getNombre(),
                            quiz.getFechaFinalizacion(),
                            redondear(m.porcentajeAprobacion()),
                            lista.size());
                })
                .toList();
    }

    private Instant fechaFin(List<Intento> intentosDeQuiz) {
        return intentosDeQuiz.get(0).getQuiz().getFechaFinalizacion();
    }

    // Estadisticas de un Quiz

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

        List<IntentoDetalle> detalles = detalleRepository
                .findByIntento_IdIn(idsDe(intentos));

        return calcularEstadisticasQuiz(quiz, intentos, detalles);
    }

    // Estadisticas de un grupo

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

        List<IntentoDetalle> detalles = detalleRepository
                .findByIntento_IdIn(idsDe(intentos));

        return calcularEstadisticasGrupo(grupo, intentos, detalles);
    }

    // Resumen de todos los grupos permitidos

    @Override
    public List<EstadisticasGrupoResumenDTO> obtenerResumenGrupos(User usuario) {

        List<Grupo> grupos = obtenerGruposPermitidos(usuario).stream()
                .filter(Grupo::isActivo)
                .toList();

        if (grupos.isEmpty()) {
            return List.of();
        }

        List<String> grupoIds = grupos.stream()
                .map(Grupo::getId)
                .toList();

        List<Intento> intentos = intentoRepository
                .findByQuiz_Grupo_IdInAndFechaEnvioIsNotNull(grupoIds);

        Map<String, List<IntentoDetalle>> detallesPorIntento = intentos.isEmpty()
                ? Map.of()
                : agruparDetalles(detalleRepository.findByIntento_IdIn(idsDe(intentos)));

        Map<String, List<Intento>> intentosPorGrupo = intentos.stream()
                .filter(intento -> intento.getQuiz() != null
                        && intento.getQuiz().getGrupo() != null)
                .collect(Collectors.groupingBy(
                        intento -> intento.getQuiz().getGrupo().getId()));

        return grupos.stream()
                .map(grupo -> calcularResumenGrupo(
                        grupo,
                        intentosPorGrupo.getOrDefault(grupo.getId(), List.of()),
                        detallesPorIntento))
                .sorted(Comparator.comparing(EstadisticasGrupoResumenDTO::nombreGrupo))
                .toList();
    }

    private EstadisticasGrupoResumenDTO calcularResumenGrupo(
            Grupo grupo,
            List<Intento> intentos,
            Map<String, List<IntentoDetalle>> detallesPorIntento) {

        Metricas m = calcularMetricas(intentos, detallesPorIntento);

        Persona docente = grupo.getDocente();
        Materia materia = grupo.getMateria();

        return new EstadisticasGrupoResumenDTO(
                grupo.getId(),
                grupo.getNombre(),
                grupo.getClaveGrupo(),
                grupo.getSemestre(),
                docente != null ? docente.getId() : null,
                docente != null ? docente.getNombre() : null,
                docente != null ? docente.getApellidoPaterno() : null,
                docente != null ? docente.getApellidoMaterno() : null,
                materia != null ? materia.getId() : null,
                materia != null ? materia.getNombre() : null,
                redondear(m.porcentajeAprobacion()),
                m.alumnosEvaluados().size(),
                intentos.size(),
                m.incidenciasFoco());
    }

    // Estadisticas por plantel

    @Override
    public List<EstadisticasPlantelDTO> obtenerEstadisticasPlanteles(User usuario) {

        List<Grupo> grupos = obtenerGruposPermitidos(usuario);

        if (grupos.isEmpty()) {
            return List.of();
        }

        List<String> grupoIds = grupos.stream()
                .map(Grupo::getId)
                .toList();

        List<Intento> intentos = intentoRepository
                .findByQuiz_Grupo_IdInAndFechaEnvioIsNotNull(grupoIds);

        if (intentos.isEmpty()) {
            return List.of();
        }

        Map<String, List<IntentoDetalle>> detallesPorIntento = agruparDetalles(
                detalleRepository.findByIntento_IdIn(idsDe(intentos)));

        Map<Long, List<Intento>> intentosPorPlantel = intentos.stream()
                .filter(intento -> intento.getQuiz() != null
                        && intento.getQuiz().getGrupo() != null
                        && intento.getQuiz().getGrupo().getPlantel() != null)
                .collect(Collectors.groupingBy(
                        intento -> intento.getQuiz().getGrupo().getPlantel().getId()));

        return intentosPorPlantel.values().stream()
                .map(lista -> {
                    Plantel plantel = lista.get(0).getQuiz().getGrupo().getPlantel();
                    Metricas m = calcularMetricas(lista, detallesPorIntento);

                    return new EstadisticasPlantelDTO(
                            plantel.getId(),
                            plantel.getNombre(),
                            redondear(m.porcentajeAprobacion()),
                            m.alumnosEvaluados().size(),
                            lista.size(),
                            m.incidenciasFoco());
                })
                .sorted(Comparator.comparing(EstadisticasPlantelDTO::nombrePlantel))
                .toList();
    }

    // Permisos de las estadisticas

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

    // Cálculos

    private record Metricas(
            long respuestasCorrectas,
            long preguntasTotales,
            long incidenciasFoco,
            long intentosAprobados,
            long intentosReprobados,
            long intentosLimpios,
            long totalIntentos,
            Set<String> alumnosEvaluados) {

        double promedioGeneral() {
            return preguntasTotales == 0
                    ? 0
                    : (respuestasCorrectas * 100.0) / preguntasTotales;
        }

        double porcentajeAprobacion() {
            long conCalificacion = intentosAprobados + intentosReprobados;
            return conCalificacion == 0
                    ? 0
                    : (intentosAprobados * 100.0) / conCalificacion;
        }

        double porcentajeReprobacion() {
            long conCalificacion = intentosAprobados + intentosReprobados;
            return conCalificacion == 0
                    ? 0
                    : (intentosReprobados * 100.0) / conCalificacion;
        }

        double indiceFocoLimpio() {
            return totalIntentos == 0
                    ? 0
                    : (intentosLimpios * 100.0) / totalIntentos;
        }
    }

    private List<String> idsDe(List<Intento> intentos) {
        return intentos.stream()
                .map(Intento::getId)
                .toList();
    }

    private Map<String, List<IntentoDetalle>> agruparDetalles(
            List<IntentoDetalle> detalles) {

        return detalles.stream()
                .filter(detalle -> detalle.getIntento() != null)
                .collect(Collectors.groupingBy(
                        detalle -> detalle.getIntento().getId()));
    }

    private Metricas calcularMetricas(
            List<Intento> intentos,
            Map<String, List<IntentoDetalle>> detallesPorIntento) {

        long respuestasCorrectas = 0;
        long preguntasTotales = 0;
        long incidenciasFoco = 0;
        long intentosAprobados = 0;
        long intentosReprobados = 0;
        long intentosLimpios = 0;

        Set<String> alumnosEvaluados = new HashSet<>();

        for (Intento intento : intentos) {

            List<IntentoDetalle> detallesIntento = detallesPorIntento
                    .getOrDefault(intento.getId(), List.of());

            long correctasIntento = 0;
            long incidenciasIntento = 0;

            for (IntentoDetalle detalle : detallesIntento) {

                if (Boolean.TRUE.equals(detalle.getEsCorrecto())) {
                    correctasIntento++;
                }

                incidenciasIntento += Math.max(0, detalle.getCambiosPantalla());
            }

            long preguntasIntento = detallesIntento.size();

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

        return new Metricas(
                respuestasCorrectas,
                preguntasTotales,
                incidenciasFoco,
                intentosAprobados,
                intentosReprobados,
                intentosLimpios,
                intentos.size(),
                alumnosEvaluados);
    }

    private EstadisticasGeneralesDTO calcularEstadisticasGenerales(
            List<Intento> intentos,
            List<IntentoDetalle> detalles) {

        Metricas m = calcularMetricas(intentos, agruparDetalles(detalles));

        return new EstadisticasGeneralesDTO(
                redondear(m.promedioGeneral()),
                redondear(m.porcentajeAprobacion()),
                redondear(m.porcentajeReprobacion()),
                redondear(m.indiceFocoLimpio()),
                m.alumnosEvaluados().size(),
                intentos.size(),
                m.incidenciasFoco(),
                m.intentosAprobados(),
                m.intentosReprobados());
    }

    private EstadisticasQuizDTO calcularEstadisticasQuiz(
            Quiz quiz,
            List<Intento> intentos,
            List<IntentoDetalle> detalles) {

        Metricas m = calcularMetricas(intentos, agruparDetalles(detalles));

        return new EstadisticasQuizDTO(
                quiz.getId(),
                quiz.getNombre(),
                redondear(m.promedioGeneral()),
                redondear(m.porcentajeAprobacion()),
                redondear(m.porcentajeReprobacion()),
                redondear(m.indiceFocoLimpio()),
                m.alumnosEvaluados().size(),
                intentos.size(),
                m.incidenciasFoco(),
                m.intentosAprobados(),
                m.intentosReprobados());
    }

    private EstadisticasGrupoDTO calcularEstadisticasGrupo(
            Grupo grupo,
            List<Intento> intentos,
            List<IntentoDetalle> detalles) {

        Metricas m = calcularMetricas(intentos, agruparDetalles(detalles));

        int quizzesAplicados = (int) intentos.stream()
                .filter(intento -> intento.getQuiz() != null)
                .map(intento -> intento.getQuiz().getId())
                .distinct()
                .count();

        return new EstadisticasGrupoDTO(
                grupo.getId(),
                grupo.getNombre(),
                grupo.getClaveGrupo(),
                grupo.getSemestre(),
                redondear(m.promedioGeneral()),
                redondear(m.porcentajeAprobacion()),
                redondear(m.porcentajeReprobacion()),
                redondear(m.indiceFocoLimpio()),
                m.alumnosEvaluados().size(),
                quizzesAplicados,
                intentos.size(),
                m.incidenciasFoco(),
                m.intentosAprobados(),
                m.intentosReprobados());
    }

    private EstadisticasMateriaDTO calcularEstadisticasMateria(
            Materia materia,
            List<Intento> intentos,
            Map<String, List<IntentoDetalle>> detallesPorIntento) {

        Metricas m = calcularMetricas(intentos, detallesPorIntento);

        return new EstadisticasMateriaDTO(
                materia.getId(),
                materia.getNombre(),
                redondear(m.porcentajeAprobacion()),
                intentos.size(),
                m.intentosAprobados(),
                m.incidenciasFoco());
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