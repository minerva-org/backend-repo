package com.minerva.api.dto;

import java.time.Instant;

import com.minerva.api.User.Roles;
import lombok.*;

@Data 
@Builder
@AllArgsConstructor 
@NoArgsConstructor
public class PersonaRequestDTO {
    private String id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String email;
    private Roles rol;
    private Long plantelId;
    private String calle;
    private String colonia;
    private String municipio;
    private String estado;
    private String codigoPostal;
    private String numeroTelefono;
    private String password;
    // private String username;
    private Boolean activo;

    // Docente
    private String carrera;
    private String tituloEdu;

    // Alumno
    private String nombreTutor;
    private String numeroTutor;
    private Instant fechaIngreso;
}
