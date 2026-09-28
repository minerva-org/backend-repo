package com.minerva.api.mapper;

import com.minerva.api.dto.MateriaDTO;
import com.minerva.api.dto.PlantelDTO;
import com.minerva.api.dto.UnidadDTO;
import com.minerva.api.dto.UniversidadDTO;
import com.minerva.api.model.Materia;
import com.minerva.api.model.Plantel;
import com.minerva.api.model.Unidad;
import com.minerva.api.model.Universidad;

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

    public static UniversidadDTO toDTO(Universidad universidad){
        if(universidad == null) return null;

        return UniversidadDTO.builder()
            .id(universidad.getId())
            .nombre(universidad.getNombre())
            .build();
    }

    public static MateriaDTO toDTO(Materia materia){
        if(materia == null) return null;

        return MateriaDTO.builder()
            .id(materia.getId())
            .nombre(materia.getNombre())
            .prefijo(materia.getPrefijo())
            .planEstudioId(materia.getPlanEstudio().getId())
            .build();
    }

    public static UnidadDTO toDTO(Unidad unidad){
        if(unidad == null) return null;

        return UnidadDTO.builder()
        .id(unidad.getId())
        .nombre(unidad.getNombre())
        .idMateria(unidad.getMateria().getId())
        .build();
    }
}
