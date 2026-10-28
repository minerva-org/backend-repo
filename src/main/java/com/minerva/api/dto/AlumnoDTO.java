package com.minerva.api.dto;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlumnoDTO extends PersonaRequestDTO {

    private String nombreTutor;
    private String numeroTutor;
    private Instant fechaIngreso;
    private Float promedio;
}