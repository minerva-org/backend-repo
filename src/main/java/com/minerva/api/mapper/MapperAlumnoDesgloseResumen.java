package com.minerva.api.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.minerva.api.dto.AlumnoDesgloseDTO;
import com.minerva.api.dto.RespuestaAlumnoDTO;
import com.minerva.api.model.QuizAlumnoPreguntaResumen;

public class MapperAlumnoDesgloseResumen {
    private static final String RESULTADO_CORRECTA = "CORRECTA";
        
    public static List<AlumnoDesgloseDTO> toDesglose(List<QuizAlumnoPreguntaResumen> filas) {
        Map<String, List<QuizAlumnoPreguntaResumen>> porAlumno = new LinkedHashMap<>();
        for (QuizAlumnoPreguntaResumen f : filas) {
            porAlumno.computeIfAbsent(f.getAlumnoId(), k -> new ArrayList<>()).add(f);
        }

        return porAlumno.values().stream()
                .map(MapperAlumnoDesgloseResumen::toAlumnoDesglose)
                .toList();
    }

    private static AlumnoDesgloseDTO toAlumnoDesglose(List<QuizAlumnoPreguntaResumen> filasAlumno) {
        QuizAlumnoPreguntaResumen primera = filasAlumno.get(0);

        long total = filasAlumno.size();
        long correctas = filasAlumno.stream()
                .filter(f -> RESULTADO_CORRECTA.equals(f.getResultado()))
                .count();

        List<RespuestaAlumnoDTO> respuestas = filasAlumno.stream()
                .map(MapperAlumnoDesgloseResumen::toRespuesta)
                .toList();

        return new AlumnoDesgloseDTO(
                primera.getAlumnoId(), primera.getAlumnoNombre(),
                primera.getGrupoId(), primera.getGrupoClave(), primera.getGrupoNombre(),
                primera.getIntentoId(), primera.isFinalizado(),
                correctas, total, calcularCalificacion(correctas, total),
                respuestas);
    }

    private static RespuestaAlumnoDTO toRespuesta(QuizAlumnoPreguntaResumen f) {
        return new RespuestaAlumnoDTO(
                f.getNumero(), f.getPreguntaId(), f.getPregunta(),
                f.getIdConcepto(), f.getDescripcionConcepto(),
                f.getOpcionElegida(), f.getOpcionCorrecta(),
                f.getResultado(), f.getCambiosPantalla());
    }

    /** Porcentaje redondeado a 2 decimales. */
    private static double calcularCalificacion(long correctas, long total) {
        return total == 0 ? 0 : Math.round(correctas * 10000.0 / total) / 100.0;
    }
    
}
