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
public class CurriculoGrupoDTO {
    private String idGrupo;

    @Builder.Default
    private List<UnidadGrupoDTO> unidades = new ArrayList<>();

    @Builder.Default
    private List<FuenteGrupoDTO> fuentes = new ArrayList<>();
}
