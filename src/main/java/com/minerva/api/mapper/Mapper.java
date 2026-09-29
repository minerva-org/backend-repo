package com.minerva.api.mapper;

import com.minerva.api.dto.ConceptoDTO;
import com.minerva.api.dto.MateriaDTO;
import com.minerva.api.dto.PlantelDTO;
import com.minerva.api.dto.PreguntaDTO;
import com.minerva.api.dto.QuizDTO;
import com.minerva.api.dto.QuizXPreguntaDTO;
import com.minerva.api.dto.TemaDTO;
import com.minerva.api.dto.UnidadDTO;
import com.minerva.api.dto.UniversidadDTO;
import com.minerva.api.model.Concepto;
import com.minerva.api.model.Materia;
import com.minerva.api.model.Plantel;
import com.minerva.api.model.Pregunta;
import com.minerva.api.model.Quiz;
import com.minerva.api.model.QuizXPregunta;
import com.minerva.api.model.Tema;
import com.minerva.api.model.Unidad;
import com.minerva.api.model.Universidad;

public class Mapper {
    public static PlantelDTO toDTO(Plantel plantel){
        if(plantel == null) return null;

        return PlantelDTO.builder()
            .id(plantel.getId())
            .nombre(plantel.getNombre())
            .direccion(plantel.getDireccion())
            .universidadId(plantel.getUniversidad().getId())
            .activo(plantel.getActivo())
            .build();
    }

    public static UniversidadDTO toDTO(Universidad universidad){
        if(universidad == null) return null;

        return UniversidadDTO.builder()
            .id(universidad.getId())
            .nombre(universidad.getNombre())
            .build();
    }

    public static MateriaDTO toDTO(Materia materia){
        if(materia == null) return null;

        return MateriaDTO.builder()
            .id(materia.getId())
            .nombre(materia.getNombre())
            .prefijo(materia.getPrefijo())
            .planEstudioId(materia.getPlanEstudio().getId())
            .build();
    }

    public static UnidadDTO toDTO(Unidad unidad){
        if(unidad == null) return null;

        return UnidadDTO.builder()
        .id(unidad.getId())
        .nombre(unidad.getNombre())
        .idMateria(unidad.getMateria().getId())
        .build();
    }

    public static TemaDTO toDTO(Tema tema){
        if(tema == null) return null;

        return TemaDTO.builder()
        .id(tema.getId())
        .nombre(tema.getNombre())
        .unidadId(tema.getUnidad().getId())
        .build();
    }

    public static ConceptoDTO toDTO(Concepto concepto){
        if(concepto == null) return null;

        return ConceptoDTO.builder()
        .id(concepto.getId())
        .nombre(concepto.getNombre())
        .temaId(concepto.getTema().getId())
        .build();
    }

    public static QuizDTO toDTO(Quiz quiz) {
    if (quiz == null) return null;

    return QuizDTO.builder()
        .id(quiz.getId())
        .nombre(quiz.getNombre())
        .fechaCreacion(quiz.getFechaCreacion())
        .fechaInicio(quiz.getFechaInicio())
        .fechaFinalizacion(quiz.getFechaFinalizacion())
        .grupoId(quiz.getGrupo().getId())
        .build();

    }

    public static PreguntaDTO toDTO(Pregunta pregunta) {
    if (pregunta == null) return null;

    return PreguntaDTO.builder()
        .id(pregunta.getId())
        .descripcion(pregunta.getDescripcion())
        .conceptoId(pregunta.getConcepto().getId())
        .build();
    }

    public static QuizXPreguntaDTO toDTO(QuizXPregunta quizXPregunta) {
    if (quizXPregunta == null) return null;

    return QuizXPreguntaDTO.builder()
        .id(quizXPregunta.getId())
        .quizId(quizXPregunta.getQuiz().getId())
        .preguntaId(quizXPregunta.getPregunta().getId())
        .conceptoId(quizXPregunta.getConcepto().getId())
        .build();
}
}
