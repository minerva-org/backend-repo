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
public class ConceptoGrupoDTO {
    private String id;
    private String nombre;
    private String idTemaGrupo;
    private String idConceptoOrigen;
    private Boolean activo;

    @Builder.Default
    private List<ContenidoGrupoDTO> contenidos = new ArrayList<>();
}
