package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.QuizDTO;

public interface QuizService {
    List<QuizDTO> findAll();

    List<QuizDTO> findAllByGrupoId(String grupoId);

    QuizDTO getQuizById(String quizId);

    QuizDTO saveQuiz(QuizDTO quizDTO);

    QuizDTO updateQuiz(String quizId, QuizDTO quizDTO);
    
    void deleteQuiz(String quizId);
}