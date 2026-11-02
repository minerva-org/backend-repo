package com.minerva.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class DocenteDTO extends PersonaRequestDTO {

    private String carrera;
    private String tituloEdu;
}
