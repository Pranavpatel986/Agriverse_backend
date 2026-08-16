package com.agriverse.api.learning.repository;

import com.agriverse.api.learning.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByPublicId(UUID publicId);
}
