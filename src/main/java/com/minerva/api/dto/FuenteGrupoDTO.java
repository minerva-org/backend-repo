package com.minerva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FuenteGrupoDTO {
    private String idDocumento;
    private String idGrupo;
    private DocumentoDTO documento;
}
