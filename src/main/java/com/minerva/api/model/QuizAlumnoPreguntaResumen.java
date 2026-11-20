package com.minerva.api.model;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Immutable
@Table(name = "v_quiz_alumno_pregunta")
@Getter
public class QuizAlumnoPreguntaResumen {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "id_quiz")
    private String quizId;

    @Column(name = "id_intento")
    private String intentoId;

    @Column(name = "id_alumno")
    private String alumnoId;

    @Column(name = "alumno_nombre")
    private String alumnoNombre;

    @Column(name = "finalizado")
    private boolean finalizado;

    @Column(name = "numero")
    private long numero;

    @Column(name = "id_concepto")
    private String idConcepto;

    @Column(name = "descripcion_concepto")
    private String descripcionConcepto;

    @Column(name = "id_pregunta")
    private String preguntaId;

    @Column(name = "pregunta")
    private String pregunta;

    @Column(name = "id_opcion_elegida")
    private String opcionElegidaId;

    @Column(name = "opcion_elegida")
    private String opcionElegida;

    @Column(name = "opcion_correcta")
    private String opcionCorrecta;

    @Column(name = "resultado")
    private String resultado;

    @Column(name = "cambios_pantalla")
    private int cambiosPantalla;

    @Column(name = "id_grupo")
    private String grupoId;

    @Column(name = "grupo_clave")
    private String grupoClave;

    @Column(name = "grupo_nombre")
    private String grupoNombre;
}