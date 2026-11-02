package com.minerva.api.dto;

import com.minerva.api.User.Roles;

import lombok.Builder;

@Builder 
public record PersonaDatosCompletosResponseDTO 
    (
        String id,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String email,
        Roles rol,
        String colonia,
        String municipio,
        String estado,
        String codigoPostal,
        String numeroTelefono
    ){
    
}
