package com.minerva.api.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "periodos")
public class Periodo {
    
    @Id
    @Column (name = "id")
    private String id;

    @Column(name = "descripcion", nullable = false, updatable = false)
    private String descripcion;
    
    @Column (name = "curso", nullable = false, updatable = false)
    private String curso;

    @Column (name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;


    @PrePersist
    public void generarUUID() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
