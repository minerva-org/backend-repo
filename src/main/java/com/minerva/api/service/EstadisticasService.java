package com.minerva.api.service;

import com.minerva.api.User.User;
import com.minerva.api.dto.EstadisticasGeneralesDTO;
import com.minerva.api.dto.EstadisticasQuizDTO;
import com.minerva.api.dto.EstadisticasGrupoDTO;

public interface EstadisticasService {

    EstadisticasGeneralesDTO obtenerEstadisticasGenerales(User usuario);

    EstadisticasQuizDTO obtenerEstadisticasQuiz(String quizId, User usuario);

    EstadisticasGrupoDTO obtenerEstadisticasGrupo(String grupoId, User usuario);
}