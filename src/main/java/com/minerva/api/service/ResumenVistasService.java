package com.minerva.api.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.AlumnoDesgloseDTO;
import com.minerva.api.dto.RespuestaAlumnoDTO;
import com.minerva.api.dto.ResumenGeneralDTO;
import com.minerva.api.mapper.Mapper;
import com.minerva.api.model.QuizAlumnoPreguntaResumen;
import com.minerva.api.repository.PersonaRepository;
import com.minerva.api.repository.QuizAlumnoPreguntaResumenRepository;
import com.minerva.api.repository.QuizResumenGeneralRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResumenVistasService { 

    private final QuizResumenGeneralRepository generalRepository;
    private final QuizAlumnoPreguntaResumenRepository alumnoPreguntaRepository;
    private final PersonaRepository personaRepository;

    @Transactional(readOnly = true)
    public ResumenGeneralDTO general(String quizId) {
        return generalRepository.findById(quizId)
                .map(Mapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));
    } 

    @Transactional(readOnly = true)
    public List<ResumenGeneralDTO> generalPorDocente(String docenteId){
        if (!personaRepository.existsById(docenteId)) throw new EntityNotFoundException("No se encontró un docente con el Id: " + docenteId);
        return generalRepository.findByDocenteId(docenteId)
            .stream()
            .map(Mapper::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AlumnoDesgloseDTO> alumnos(String quizId) {
        return agrupar(alumnoPreguntaRepository.findByQuizIdOrderByAlumnoNombreAscNumeroAsc(quizId));
    }

    @Transactional(readOnly = true)
    public AlumnoDesgloseDTO alumno(String quizId, String alumnoId) {
        return agrupar(alumnoPreguntaRepository.findByQuizIdAndAlumnoIdOrderByNumeroAsc(quizId, alumnoId))
                .stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("El alumno no tiene intento en este quiz"));
    }

    /** Agrupa las filas (una por pregunta) en un desglose por alumno. */
    private List<AlumnoDesgloseDTO> agrupar(List<QuizAlumnoPreguntaResumen> filas) {
        Map<String, List<QuizAlumnoPreguntaResumen>> porAlumno = new LinkedHashMap<>();
        for (QuizAlumnoPreguntaResumen f : filas) {
            porAlumno.computeIfAbsent(f.getAlumnoId(), k -> new ArrayList<>()).add(f);
        }

        return porAlumno.values().stream().map(lista -> {
            QuizAlumnoPreguntaResumen primera = lista.get(0);
            long correctas = lista.stream().filter(f -> "CORRECTA".equals(f.getResultado())).count();
            long total = lista.size();
            double calificacion = total == 0 ? 0 : Math.round(correctas * 10000.0 / total) / 100.0;

            List<RespuestaAlumnoDTO> respuestas = lista.stream()
                    .map(f -> new RespuestaAlumnoDTO(f.getNumero(), f.getPreguntaId(), f.getPregunta(),
                            f.getIdConcepto(), f.getDescripcionConcepto(),
                            f.getOpcionElegida(), f.getOpcionCorrecta(),
                            f.getResultado(), f.getCambiosPantalla()))
                    .toList();

            return new AlumnoDesgloseDTO(primera.getAlumnoId(), primera.getAlumnoNombre(),
                    primera.getGrupoId(), primera.getGrupoClave(), primera.getGrupoNombre(),
                    primera.getIntentoId(), primera.isFinalizado(),
                    correctas, total, calificacion, respuestas);
        }).toList();
    }
}