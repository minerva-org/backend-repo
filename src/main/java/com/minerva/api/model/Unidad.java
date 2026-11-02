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
@Table (name = "unidad")
public class Unidad {
    @Id
    @Column (name = "id", nullable = false, unique = true) 
    private String id;

    @Column (name = "detalles", nullable = false)
    private String nombre;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "id_materia", nullable = false)
    private Materia materia;

    @Column (name = "activo")
    private Boolean activo = true;
}
