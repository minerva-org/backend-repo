package com.minerva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
@Entity
@Table (name = "concepto")
public class Concepto {
    @Id 
    @Column (name = "id", nullable = false)
    private String id;

    @Column (name = "detalles", nullable = false)
    private String nombre;

    @JoinColumn  (name = "id_tema", nullable = false)
    @ManyToOne (fetch = FetchType.LAZY)
    private Tema tema;

}
