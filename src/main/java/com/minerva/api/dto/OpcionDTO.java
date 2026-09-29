package com.minerva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpcionDTO {
    private String id;
    private String descripcion;
    private Boolean esCorrecta;
    private String preguntaId;
}