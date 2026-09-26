package com.minerva.api.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class PlantelDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private Long universidadId;
    private Boolean activo;
}
