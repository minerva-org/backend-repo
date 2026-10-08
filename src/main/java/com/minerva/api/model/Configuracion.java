package com.minerva.api.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
@Entity 
@Table (name = "configuraciones")
public class Configuracion {
    @Id 
    @Column (name = "id", nullable = false)
    private String id;

    @Column (name = "valor", nullable = false)
    private String valor;

    @Column (name = "descripcion")
    private String descripcion;

    @Column (name = "fecha_actualizacion")
    private Instant fechaActualizacion;
}
