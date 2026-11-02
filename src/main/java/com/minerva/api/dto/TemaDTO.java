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
public class TemaDTO {
    private String id;
    private String nombre;
    private String unidadId;
    private List<ConceptoDTO> conceptos;
}
