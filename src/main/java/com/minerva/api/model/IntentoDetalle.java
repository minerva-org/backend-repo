package com.minerva.api.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

// @RequiredArgsConstructor 
// @Getter 
// @Setter 
// @Entity 
// @Table (name = "detalles_intentos", 
//         uniqueConstraints = @UniqueConstraint(columnNames = {"id_intento", "id_pregunta"}))

public class IntentoDetalle {

    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_intento", nullable = false)
    private Intento intento;

    @Column (name = "camtios_pantalla")
    private int cambiosPantalla;

    @JoinColumn (name = "id_pregunta", nullable = false)
    @ManyToOne (fetch = FetchType.LAZY)
    private Pregunta pregunta;

    @Column (name = "es_correcto")
    private Boolean esCorrecto;

    @Column (name = "respuesta")
    private String respuesta;
}
