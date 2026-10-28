package com.minerva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

// @RequiredArgsConstructor 
// @Getter 
// @Setter 
// @Entity 
// @Table (name = "intentos")
public class Intento {
    @Id 
    @Column (name = "id")
    private String id;

    @JoinColumn (name = "id_quiz", nullable = true)
    @ManyToOne (fetch = FetchType.LAZY)
    private Quiz quiz;

    @JoinColumn (name = "id_alumno", nullable = true)
    @ManyToOne (fetch = FetchType.LAZY)
    private Alumno alumno;
}
