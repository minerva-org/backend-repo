package com.minerva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
@Entity 
@Table (name = "grupo")
public class Grupo {
    
    @Id 
    @Column (name = "id", nullable = false)
    private String id;
    
}
