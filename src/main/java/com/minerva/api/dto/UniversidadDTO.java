package com.minerva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class UniversidadDTO {
    private Long id;
    private String nombre;
    private Boolean activo;
    
}
