package com.agriverse.api.learning.repository;

import com.agriverse.api.learning.entity.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {
    List<QuizOption> findByQuestionId(Long questionId);
    List<QuizOption> findByQuestionIdIn(List<Long> questionIds);
}
