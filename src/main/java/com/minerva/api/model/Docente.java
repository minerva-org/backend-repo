package com.minerva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity 
@Table (name = "docentes")
@Getter @Setter 
public class Docente extends Persona {
    @Column (name = "carrera", length = 50)
    private String carrera;

    @Column (name = "titulo_edu")
    private String tituloEdu;
}
