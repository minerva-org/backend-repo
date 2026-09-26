package com.minerva.api.mapper;

import com.minerva.api.dto.PlantelDTO;
import com.minerva.api.model.Plantel;

public class Mapper {
    public static PlantelDTO toDTO(Plantel plantel){
        if(plantel == null) return null;

        return PlantelDTO.builder()
            .id(plantel.getId())
            .nombre(plantel.getNombre())
            .direccion(plantel.getDireccion())
            .universidadId(plantel.getUniversidad().getId())
            .activo(plantel.getActivo())
            .build();
    }
}
