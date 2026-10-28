package com.minerva.api.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "alumnos")
// @PrimaryKeyJoinColumn(name = "id_persona")
@Getter @Setter
public class Alumno extends Persona {

    @Column (name = "tutor")
    private String nombreTutor;

    @Column (name = "numero_tutor")
    private String numeroTutor;

    @Column (name = "fecha_ingreso")
    private Instant fechaIngreso = Instant.now();

    @Column (name = "promedio_general")
    public Float promedio;
    
}