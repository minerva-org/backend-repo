package com.minerva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data 
@Entity 
@Table (name = "universidad")
public class Universidad {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column (name = "detalles")
    private String nombre;

}
