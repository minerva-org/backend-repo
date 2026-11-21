package com.minerva.api.model;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Immutable
@Table(name = "v_quiz_resumen_general")
@Getter
public class QuizResumenGeneral {

    @Id
    @Column(name = "id_quiz")
    private String quizId;

    @Column(name = "num_preguntas")
    private long numPreguntas;

    @Column(name = "num_intentos")
    private long numIntentos;

    @Column(name = "num_respondieron")
    private long numRespondieron;

    @Column(name = "num_en_curso")
    private long numEnCurso;

    @Column(name = "num_aprobados")
    private long numAprobados;

    @Column(name = "num_reprobados")
    private long numReprobados;

    @Column(name = "promedio")
    private double promedio;

    @Column(name = "calificacion_maxima")
    private double calificacionMaxima;

    @Column(name = "calificacion_minima")
    private double calificacionMinima;

    @Column(name = "duracion_promedio_segundos")
    private double duracionPromedioSegundos;

    @Column(name = "id_grupo")
    private String grupoId;

    @Column(name = "id_docente")
    private String docenteId;

    @Column(name = "docente_nombre")
    private String docenteNombre;

    @Column(name = "grupo_clave")
    private String grupoClave;

    @Column(name = "grupo_nombre")
    private String grupoNombre;
}