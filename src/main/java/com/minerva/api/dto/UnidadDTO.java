package com.minerva.api.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class UnidadDTO {
    private String id;
    private String nombre;
    private String idMateria;
    private List<TemaDTO> temas;
}
