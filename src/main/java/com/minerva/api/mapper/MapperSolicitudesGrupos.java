package com.minerva.api.mapper;

import com.minerva.api.dto.SolicitudesGruposResponseDTO;
import com.minerva.api.model.Alumno;
import com.minerva.api.model.Grupo;
import com.minerva.api.model.SolicitudesGrupos;

public class MapperSolicitudesGrupos {

    public static SolicitudesGruposResponseDTO toDTO(SolicitudesGrupos solicitud, Alumno alumno, Grupo grupo) {
        if (alumno == null || grupo == null || solicitud == null)
            return null;

        String nombre = alumno.getNombre() + " " + alumno.getApellidoPaterno();
        return new SolicitudesGruposResponseDTO(
                solicitud.getId(),
                grupo.getId(), grupo.getNombre(),
                alumno.getId(), nombre);

    }
}
