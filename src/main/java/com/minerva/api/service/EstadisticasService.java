package com.minerva.api.service;

import java.util.List;

import com.minerva.api.User.User;
import com.minerva.api.dto.EstadisticasDTO.*;

public interface EstadisticasService {

    EstadisticasGeneralesDTO obtenerEstadisticasGenerales(User usuario);

    EstadisticasQuizDTO obtenerEstadisticasQuiz(String quizId, User usuario);

    EstadisticasGrupoDTO obtenerEstadisticasGrupo(String grupoId, User usuario);

    List<EstadisticasMateriaDTO> obtenerEstadisticasMateria(User usuario);

    List<EstadisticasDocentesDTO> obtenerEstadisticasDocentes(User usuario);

    List<EstadisticasTendenciaDTO> obtenerTendenciaAprobacion(User usuario);

    List<EstadisticasTendenciaDTO> obtenerTendenciaAprobacionGrupo(
            String grupoId, User usuario);

    List<EstadisticasGrupoResumenDTO> obtenerResumenGrupos(User usuario);

    List<EstadisticasPlantelDTO> obtenerEstadisticasPlanteles(User usuario);

    EstadisticasPlantelDTO obtenerEstadisticasPlantel(Long plantelId, User usuario);
}