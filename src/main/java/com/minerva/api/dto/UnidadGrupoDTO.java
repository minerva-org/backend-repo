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
public class UnidadGrupoDTO {
    private String id;
    private String nombre;
    private String idMateria;
    private String idGrupo;
    private String idUnidadOrigen;
    private Boolean activo;

    @Builder.Default
    private List<TemaGrupoDTO> temas = new ArrayList<>();
}
