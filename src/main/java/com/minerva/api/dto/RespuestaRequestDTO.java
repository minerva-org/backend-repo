package com.minerva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class RespuestaRequestDTO {
        private String preguntaId;
        private String opcionId;
        private int cambiosPantalla;
    }
