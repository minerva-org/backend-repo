package com.minerva.api.mapper.mapperResumenesGenerales;

import com.minerva.api.dto.ResumenesGeneralesDTO.ResumenGeneralDocenteDTO;
import com.minerva.api.model.QuizResumenGeneral;

public class MapperResumenGeneralDocente {
        public static ResumenGeneralDocenteDTO toDTO(QuizResumenGeneral g){
        if(g == null) return null;

        return new ResumenGeneralDocenteDTO(
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
                        g.getDocenteNombre());
    }
    
}
