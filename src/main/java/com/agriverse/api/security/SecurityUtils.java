package com.agriverse.api.security;

import com.agriverse.api.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/** Convenience accessors for the currently authenticated principal. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UserPrincipal> currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public static UserPrincipal requireCurrentPrincipal() {
        return currentPrincipal().orElseThrow(() -> new UnauthorizedException("Authentication required"));
    }

    public static Long currentUserId() {
        return requireCurrentPrincipal().getId();
    }

    public static UUID currentUserPublicId() {
        return requireCurrentPrincipal().getPublicId();
    }

    public static boolean hasRole(String role) {
        return currentPrincipal().map(p -> p.getRole().equalsIgnoreCase(role)).orElse(false);
    }

    public static boolean hasAnyRole(String... roles) {
        return currentPrincipal().map(p -> {
            for (String r : roles) {
                if (p.getRole().equalsIgnoreCase(r)) {
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }
}
