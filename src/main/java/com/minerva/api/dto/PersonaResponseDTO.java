package com.minerva.api.dto;

import com.minerva.api.User.Roles;
import lombok.*;

@Data 
@Builder
@AllArgsConstructor 
@NoArgsConstructor

public class PersonaResponseDTO {
    private String nombre;
    private String apellido;
    private String email;
    private Roles rol;
    private Boolean activo;
    private Long plantelId;
    private String username;
}
