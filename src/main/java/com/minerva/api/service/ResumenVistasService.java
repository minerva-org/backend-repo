package com.minerva.api.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minerva.api.dto.AlumnoDesgloseDTO;
import com.minerva.api.dto.RespuestaAlumnoDTO;
import com.minerva.api.dto.ResumenesGeneralesDTO.ResumenGeneralDTO;
import com.minerva.api.dto.ResumenesGeneralesDTO.ResumenGeneralDocenteDTO;
import com.minerva.api.dto.ResumenesGeneralesDTO.ResumenGeneralGrupoDTO;
import com.minerva.api.dto.ResumenesGeneralesDTO.ResumenGeneralPlantelDTO;
import com.minerva.api.mapper.MapperAlumnoDesgloseResumen;
import com.minerva.api.mapper.mapperResumenesGenerales.MapperResumenGeneral;
import com.minerva.api.mapper.mapperResumenesGenerales.MapperResumenGeneralDocente;
import com.minerva.api.mapper.mapperResumenesGenerales.MapperResumenGeneralGrupo;
import com.minerva.api.mapper.mapperResumenesGenerales.MapperResumenGeneralPlantel;
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
    public List<ResumenGeneralDTO> general() {
        return generalRepository.findAll()
                .stream()
                .map(MapperResumenGeneral::toDTO)
                .toList();
    } 

    @Transactional(readOnly = true)
    public ResumenGeneralDTO generalPorQuiz(String quizId) {
        return generalRepository.findById(quizId)
                .map(MapperResumenGeneral::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Quiz no encontrado con el id: " + quizId));
    } 

    @Transactional(readOnly = true)
    public List<ResumenGeneralDocenteDTO> generalPorDocente(String docenteId){
        if (!personaRepository.existsById(docenteId)) throw new EntityNotFoundException("No se encontró un docente con el Id: " + docenteId);
        return generalRepository.findByDocenteId(docenteId)
            .stream()
            .map(MapperResumenGeneralDocente::toDTO)
            .toList();
    } 

    @Transactional(readOnly = true)
    public List<ResumenGeneralGrupoDTO> generalPorGrupo(String grupoId){
        if (!generalRepository.existsByGrupoId(grupoId)) throw new EntityNotFoundException("No se encontró un grupo con el Id: " + grupoId);
        return generalRepository.findByGrupoId(grupoId)
            .stream()
            .map(MapperResumenGeneralGrupo::toDTO)
            .toList();
    } 

    @Transactional(readOnly = true)
    public List<ResumenGeneralPlantelDTO> generalPorPlantel(String plantelId){
        if (!generalRepository.existsByPlantelId(plantelId)) throw new EntityNotFoundException("No se encontró un quiz en el plantel con el Id: " + plantelId);
        return generalRepository.findByPlantelId(plantelId)
            .stream()
            .map(MapperResumenGeneralPlantel::toDTO)
            .toList();
    } 

    @Transactional(readOnly = true)
    public List<AlumnoDesgloseDTO> alumnos(String quizId) {
        return agrupar(alumnoPreguntaRepository.findByQuizIdOrderByAlumnoNombreAscNumeroAsc(quizId));
    }

    @Transactional(readOnly = true)
    public List<AlumnoDesgloseDTO> alumnosPorGrupo(String grupoId){
        List<QuizAlumnoPreguntaResumen> filas = alumnoPreguntaRepository.findByGrupoId(grupoId);
        return MapperAlumnoDesgloseResumen.toDesglose(filas);
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