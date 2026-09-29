package com.minerva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class ConceptoDTO {
    private String id;
    private String nombre;
    private String temaId;
}
