package com.minerva.api.mapper;

import com.minerva.api.dto.AlumnoDTO;
import com.minerva.api.dto.ConceptoDTO;
import com.minerva.api.dto.DocenteDTO;
import com.minerva.api.dto.GrupoDTO;
import com.minerva.api.dto.MateriaDTO;
import com.minerva.api.dto.OpcionDTO;
import com.minerva.api.dto.PersonaRequestDTO;
import com.minerva.api.dto.PersonaResponseDTO;
import com.minerva.api.dto.PlantelDTO;
import com.minerva.api.dto.PreguntaDTO;
import com.minerva.api.dto.QuizDTO;
import com.minerva.api.dto.QuizXPreguntaDTO;
import com.minerva.api.dto.TemaDTO;
import com.minerva.api.dto.UnidadDTO;
import com.minerva.api.dto.InstitucionDTO;
import com.minerva.api.model.Alumno;
import com.minerva.api.model.Concepto;
import com.minerva.api.model.Docente;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.Materia;
import com.minerva.api.model.Opcion;
import com.minerva.api.model.Persona;
import com.minerva.api.model.Plantel;
import com.minerva.api.model.Pregunta;
import com.minerva.api.model.Quiz;
import com.minerva.api.model.QuizXPregunta;
import com.minerva.api.model.Tema;
import com.minerva.api.model.Unidad;
import com.minerva.api.model.Institucion;

public class Mapper {
    public static PlantelDTO toDTO(Plantel plantel) {
        if (plantel == null) return null;

        return PlantelDTO.builder()
            .id(plantel.getId())
            .nombre(plantel.getNombre())
            .direccion(plantel.getDireccion())
            .institucionId(plantel.getInstitucion() != null ? plantel.getInstitucion().getId() : null)
            .activo(plantel.getActivo())
            .build();
    }

    public static InstitucionDTO toDTO(Institucion institucion) {
        if (institucion == null) return null;

        return InstitucionDTO.builder()
            .id(institucion.getId())
            .nombre(institucion.getNombre())
            .activo(institucion.getActivo())
            .build();
    }

    public static PersonaRequestDTO toDTO(Persona persona) {
    PersonaRequestDTO dto = new PersonaRequestDTO();
    copiarComunes(persona, dto);
    return dto;
    }

    public static PersonaResponseDTO toResponse(Persona p) {
        PersonaResponseDTO dto = new PersonaResponseDTO();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setApellidoPaterno(p.getApellidoPaterno() != null ? p.getApellidoPaterno() : p.getApellidoMaterno());
        dto.setApellidoMaterno(p.getApellidoMaterno());
        dto.setEmail(p.getEmail());
        dto.setRol(p.getRol());
        dto.setActivo(p.getActivo());
        dto.setPlantelId(p.getPlantel().getId());   // Long
        return dto;
    }

    public static AlumnoDTO toDTO(Alumno alumno) {
        AlumnoDTO dto = new AlumnoDTO();
        copiarComunes(alumno, dto);
        dto.setNombreTutor(alumno.getNombreTutor());
        dto.setNumeroTutor(alumno.getNumeroTutor());
        dto.setFechaIngreso(alumno.getFechaIngreso());
        dto.setPromedio(alumno.getPromedio()); 
        return dto;
    }

    public static DocenteDTO toDTO(Docente docente) {
        DocenteDTO dto = new DocenteDTO();
        copiarComunes(docente, dto);
        dto.setCarrera(docente.getCarrera());
        dto.setTituloEdu(docente.getTituloEdu());
        return dto;
    }

    private static void copiarComunes(Persona p, PersonaRequestDTO dto) {
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setApellidoPaterno(p.getApellidoPaterno());
        dto.setApellidoMaterno(p.getApellidoMaterno());
        dto.setEmail(p.getEmail());
        dto.setRol(p.getRol());
        dto.setActivo(p.getActivo());
        dto.setPlantelId(p.getPlantel().getId());
        dto.setCalle(p.getCalle());
        dto.setColonia(p.getColonia());
        dto.setMunicipio(p.getMunicipio());
        dto.setEstado(p.getEstado());
        dto.setCodigoPostal(p.getCodigoPostal());
        dto.setNumeroTelefono(p.getNumeroTelefono());
    }

    

    public static MateriaDTO toDTO(Materia materia) {
        if (materia == null) return null;

        return MateriaDTO.builder()
            .id(materia.getId())
            .nombre(materia.getNombre())
            .prefijo(materia.getPrefijo())
            .planEstudioId(materia.getPlanEstudio() != null ? materia.getPlanEstudio().getId() : null)
            .activo(materia.getActivo())
            .build();
    }

    public static UnidadDTO toDTO(Unidad unidad) {
        if (unidad == null) return null;

        return UnidadDTO.builder()
            .id(unidad.getId())
            .nombre(unidad.getNombre())
            .idMateria(unidad.getMateria() != null ? unidad.getMateria().getId() : null)
            .build();
    }

    public static TemaDTO toDTO(Tema tema) {
        if (tema == null) return null;

        return TemaDTO.builder()
            .id(tema.getId())
            .nombre(tema.getNombre())
            .unidadId(tema.getUnidad() != null ? tema.getUnidad().getId() : null)
            .build();
    }

    public static ConceptoDTO toDTO(Concepto concepto) {
        if (concepto == null) return null;

        return ConceptoDTO.builder()
            .id(concepto.getId())
            .nombre(concepto.getNombre())
            .temaId(concepto.getTema() != null ? concepto.getTema().getId() : null)
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
            .grupoId(quiz.getGrupo() != null ? quiz.getGrupo().getId() : null)
            .build();
    }

    public static PreguntaDTO toDTO(Pregunta pregunta) {
        if (pregunta == null) return null;

        return PreguntaDTO.builder()
            .id(pregunta.getId())
            .descripcion(pregunta.getDescripcion())
            .conceptoId(pregunta.getConcepto() != null ? pregunta.getConcepto().getId() : null)
            .build();
    }

    public static QuizXPreguntaDTO toDTO(QuizXPregunta quizXPregunta) {
        if (quizXPregunta == null) return null;

        return QuizXPreguntaDTO.builder()
            .id(quizXPregunta.getId())
            .quizId(quizXPregunta.getQuiz() != null ? quizXPregunta.getQuiz().getId() : null)
            .preguntaId(quizXPregunta.getPregunta() != null ? quizXPregunta.getPregunta().getId() : null)
            .build();
    }

    public static OpcionDTO toDTO(Opcion opcion) {
        if (opcion == null) return null;

        return OpcionDTO.builder()
            .id(opcion.getId())
            .descripcion(opcion.getDescripcion())
            .esCorrecta(opcion.getEsCorrecta())
            .preguntaId(opcion.getPregunta() != null ? opcion.getPregunta().getId() : null)
            .build();
    }

    public static GrupoDTO toDTO(Grupo grupo) {
        if (grupo == null) return null;

        return GrupoDTO.builder()
            .id(grupo.getId())
            .claveGrupo(grupo.getClaveGrupo())
            .nombre(grupo.getNombre())
            .semestre(grupo.getSemestre())
            .activo(grupo.isActivo())
            .docenteId(grupo.getDocente() != null ? grupo.getDocente().getId() : null)
            .plantelId(grupo.getPlantel() != null ? grupo.getPlantel().getId() : null)
            .alumnosIds(grupo.getAlumnos() == null ? java.util.Collections.emptyList() : grupo.getAlumnos().stream().map(Persona::getId).toList())
            .build();
    }
}
