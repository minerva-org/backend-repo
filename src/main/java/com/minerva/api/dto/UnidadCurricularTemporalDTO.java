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
public class UnidadCurricularTemporalDTO {
    private String id;
    private String name;

    @Builder.Default
    private List<TemaCurricularTemporalDTO> topics = new ArrayList<>();
}
