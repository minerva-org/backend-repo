package com.minerva.api.mapper.mapperResumenesGenerales;

import com.minerva.api.dto.ResumenesGeneralesDTO.ResumenGeneralDTO;
import com.minerva.api.model.QuizResumenGeneral;

public class MapperResumenGeneral {
    public static ResumenGeneralDTO toDTO(QuizResumenGeneral g){
        if(g == null) return null;

        return new ResumenGeneralDTO(
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
                        g.getDuracionPromedioSegundos(),
                        g.getDocenteId(), 
                        g.getDocenteNombre(),
                        g.getPlantelId(),
                        g.getPlantelNombre());
    }
}
