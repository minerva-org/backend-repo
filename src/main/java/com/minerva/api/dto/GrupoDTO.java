package com.minerva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GrupoDTO {
    private String id;
    private String claveGrupo;
    private String nombre;
    private String semestre;
    private String docenteId;
}