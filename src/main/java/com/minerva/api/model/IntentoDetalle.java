package com.minerva.api.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor 
@Getter 
@Setter 
@Entity 
@Table (name = "detalles_intentos", 
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_intento", "id_pregunta"}))

public class IntentoDetalle {

    @Id 
    @Column (name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_intento", nullable = false)
    private Intento intento;

    @Column (name = "cambios_pantalla")
    private int cambiosPantalla;

    @JoinColumn (name = "id_pregunta", nullable = false)
    @ManyToOne (fetch = FetchType.LAZY)
    private Pregunta pregunta;

    @Column (name = "es_correcto")
    private Boolean esCorrecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion")
    private Opcion opcionSeleccionada;

    @PrePersist
    private void generarId() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
