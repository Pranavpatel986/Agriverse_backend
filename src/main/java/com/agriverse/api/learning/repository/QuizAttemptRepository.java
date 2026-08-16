package com.agriverse.api.learning.repository;

import com.agriverse.api.learning.entity.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Page<QuizAttempt> findByQuizIdAndUserIdOrderByAttemptedAtDesc(Long quizId, Long userId, Pageable pageable);
}
