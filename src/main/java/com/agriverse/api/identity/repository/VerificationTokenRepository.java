package com.agriverse.api.identity.repository;

import com.agriverse.api.identity.entity.VerificationToken;
import com.agriverse.api.identity.entity.VerificationTokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenHashAndType(String tokenHash, VerificationTokenType type);
}
