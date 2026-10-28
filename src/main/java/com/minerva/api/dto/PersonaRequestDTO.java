package com.minerva.api.dto;

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
    private Boolean activo;
    private String calle;
    private String colonia;
    private String municipio;
    private String estado;
    private String codigoPostal;
    private String numeroTelefono;
    private String password;
}
