package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.PreguntaDTO;
import com.minerva.api.dto.QuizXPreguntaDTO;
import com.minerva.api.User.User;

public interface QuizXPreguntaService {
    List<QuizXPreguntaDTO> findAll();

    QuizXPreguntaDTO getQuizXPreguntaById(String id);

    QuizXPreguntaDTO saveQuizXPregunta(QuizXPreguntaDTO dto);

    QuizXPreguntaDTO updateQuizXPregunta(String id, QuizXPreguntaDTO dto);
    
    void deleteQuizXPregunta(String id);

    List<PreguntaDTO> findPreguntasByQuizId(String quizId);

    List<PreguntaDTO> findPreguntasByQuizId(String quizId, User usuario);
}