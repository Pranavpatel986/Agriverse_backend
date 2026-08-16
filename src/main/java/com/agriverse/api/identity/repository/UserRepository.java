package com.agriverse.api.identity.repository;

import com.agriverse.api.identity.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where lower(u.email) = lower(:email) and u.deletedAt is null")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByPublicIdAndDeletedAtIsNull(UUID publicId);

    @Query("select u from User u where (:role is null or u.role.name = :role) " +
            "and (:status is null or u.status = :status) and u.deletedAt is null")
    Page<User> search(@Param("role") String role, @Param("status") com.agriverse.api.identity.entity.UserStatus status, Pageable pageable);
}
