package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.QuizDTO;
import com.minerva.api.User.User;

public interface QuizService {
    List<QuizDTO> findAll();

    List<QuizDTO> findAll(User usuario);

    List<QuizDTO> findAllByGrupoId(String grupoId);

    List<QuizDTO> findAllByGrupoId(String grupoId, User usuario);

    List<QuizDTO> findAllByAlumnoId(String alumnoId);

    QuizDTO getQuizById(String quizId);

    QuizDTO getQuizById(String quizId, User usuario);

    QuizDTO saveQuiz(QuizDTO quizDTO);

    QuizDTO saveQuiz(QuizDTO quizDTO, User usuario);

    QuizDTO updateQuiz(String quizId, QuizDTO quizDTO);

    QuizDTO updateQuiz(String quizId, QuizDTO quizDTO, User usuario);

    void deleteQuiz(String quizId);

    void deleteQuiz(String quizId, User usuario);
}