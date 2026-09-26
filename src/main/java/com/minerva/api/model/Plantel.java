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
@Table (name = "plantel")
public class Plantel {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id", nullable = false, updatable = false)
    private Long Id;

    @Column (name = "detalles", nullable = false)
    private String nombre;

    @Column (name = "direccion", nullable = false)
    private String direccion;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "id_universidad", nullable = false)
    private Universidad universidad;

    @Column (name = "activo")
    private Boolean activo = true;
}
