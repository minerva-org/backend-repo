package com.minerva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "opciones")
public class Opcion {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "es_correcta", nullable = false)
    private Boolean esCorrecta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;
}