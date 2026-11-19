package com.minerva.api.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;

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
@Table (name = "quiz")
public class Quiz {
    
    @Id 
    @Column (name = "id", nullable = false)
    private String id;

    @Column (name = "detalles", nullable = false)
    private String nombre;

    @CreatedDate 
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio;

    @Column(name = "fecha_finalizacion", nullable = false)
    private Instant fechaFinalizacion;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "id_grupo", nullable = false)
    private Grupo grupo;
    
}
