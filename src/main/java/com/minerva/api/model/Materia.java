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
@Table (name = "materia")
public class Materia {
    
    @Id 
    @Column (name = "id", nullable = false, unique = true)
    private String id;

    @Column (name = "detalles", nullable = false)
    private String nombre;

    @Column (name = "prefijo", nullable = false)
    private String prefijo;

    @JoinColumn (name = "id_planEstudio", nullable = true)
    @ManyToOne (fetch = FetchType.LAZY)
    private PlanEstudio planEstudio;

    @Column (name = "activo", nullable = false)
    private Boolean activo = true;
}
