package com.agriverse.api.learning.repository;

import com.agriverse.api.learning.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizIdOrderByQuestionOrderAsc(Long quizId);
}
