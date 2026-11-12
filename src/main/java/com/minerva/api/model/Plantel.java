package com.minerva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data 
@Entity 
@Table (name = "planteles")
public class Plantel {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id", nullable = false, updatable = false)
    private Long Id;

    @Column (name = "detalles", nullable = false)
    private String nombre;

    @Column (name = "calle", nullable = false)
    private String calle;

    @Column (name = "colonia", nullable = false)
    private String colonia;

    @Column (name = "municipio", nullable = false)
    private String municipio;

    @Column (name = "estado", nullable = false)
    private String estado;

    @Column (name = "codigo_postal", nullable = false)
    private String codigoPostal;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "id_institucion", nullable = false)
    private Institucion institucion;

    @Column (name = "activo")
    private Boolean activo = true;
}
