package com.minerva.api.mapper.mapperResumenesGenerales;

import com.minerva.api.dto.ResumenesGeneralesDTO.ResumenGeneralGrupoDTO;
import com.minerva.api.model.QuizResumenGeneral;

public class MapperResumenGeneralGrupo {
            public static ResumenGeneralGrupoDTO toDTO(QuizResumenGeneral g){
        if(g == null) return null;

        return new ResumenGeneralGrupoDTO(
                        g.getQuizId(), 
                        g.getGrupoId(), 
                        g.getGrupoClave(), 
                        g.getGrupoNombre(),
                        g.getNumPreguntas(), 
                        g.getNumIntentos(), 
                        g.getNumRespondieron(),
                        g.getNumEnCurso(), 
                        g.getNumAprobados(), 
                        g.getNumReprobados(),
                        g.getPromedio(), 
                        g.getCalificacionMaxima(),
                        g.getCalificacionMinima(), 
                        g.getDuracionPromedioSegundos());
    }
}
