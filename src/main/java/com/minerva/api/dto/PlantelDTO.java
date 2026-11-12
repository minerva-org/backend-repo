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
    private String calle;        
    private String colonia;      
    private String municipio;    
    private String estado;
    private String codigoPostal;
    private Long institucionId;
    private Boolean activo;
}
