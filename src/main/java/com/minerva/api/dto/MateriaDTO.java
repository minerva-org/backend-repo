package com.minerva.api.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class MateriaDTO {
    private String id;
    private String nombre;
    private String prefijo;
    private String planEstudioId;
    private List<UnidadDTO> unidades;
    private Boolean activo;
}
