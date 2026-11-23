package com.minerva.api.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "solicitudes_grupos")
public class SolicitudesGrupos {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne (fetch = FetchType.LAZY)
    @Column(name = "id_grupo")
    private Grupo grupoId;

    
    @ManyToOne (fetch = FetchType.LAZY)
    @Column(name = "id_alumno")
    private Alumno alumnoId;
    
    @PrePersist
    public void generarUUID() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
