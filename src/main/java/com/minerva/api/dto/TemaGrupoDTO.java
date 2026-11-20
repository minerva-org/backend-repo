package com.minerva.api.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemaGrupoDTO {
    private String id;
    private String clave;
    private String nombre;
    private String idUnidadGrupo;
    private String idTemaOrigen;
    private Boolean activo;

    @Builder.Default
    private List<ConceptoGrupoDTO> conceptos = new ArrayList<>();
}
