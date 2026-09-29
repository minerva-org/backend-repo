package com.minerva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data 
@Entity 
@Table (name = "plan_estudio")
public class PlanEstudio {
    @Id 
    @Column (name = "detalles")
    private String id;
}
