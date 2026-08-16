package com.agriverse.api.identity.service;

import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ConflictException;
import com.agriverse.api.common.exception.ForbiddenException;
import com.agriverse.api.common.exception.UnauthorizedException;
import com.agriverse.api.common.service.MailService;
import com.agriverse.api.identity.dto.*;
import com.agriverse.api.identity.entity.*;
import com.agriverse.api.identity.repository.*;
import com.agriverse.api.security.JwtService;
import com.agriverse.api.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Backs the Authentication endpoint group (REST API Specification, Section 1).
 * Access tokens are short-lived stateless JWTs; refresh tokens are opaque,
 * persisted hashed (never in plaintext), and revocable — per SAD Section 3.2.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long EMAIL_VERIFICATION_TTL_HOURS = 48;
    private static final long PASSWORD_RESET_TTL_MINUTES = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already registered");
        }
        Role readerRole = roleRepository.findByName(Role.READER)
                .orElseThrow(() -> new IllegalStateException("READER role missing — check RBAC seed data"));

        User user = new User();
        user.setFullName(sanitize(request.fullName()));
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(readerRole);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        userRepository.save(user);

        issueVerificationToken(user);

        return new RegisterResponse(user.getPublicId(), user.getFullName(), user.getEmail(), "pending_verification");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ForbiddenException("Account suspended");
        }
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new ForbiddenException("Account not yet verified");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = issueRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, jwtService.accessTokenTtlSeconds(),
                new UserSummary(user.getPublicId(), user.getFullName(), user.getRole().getName()));
    }

    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        String hash = jwtService.hashToken(request.refreshToken());
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalid, expired, or revoked"));
        if (!token.isValid()) {
            throw new UnauthorizedException("Refresh token invalid, expired, or revoked");
        }
        UserPrincipal principal = new UserPrincipal(token.getUser());
        String accessToken = jwtService.generateAccessToken(principal);
        return new RefreshTokenResponse(accessToken, jwtService.accessTokenTtlSeconds());
    }

    @Transactional
    public void logout(Long currentUserId, String rawRefreshToken) {
        String hash = jwtService.hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            if (!token.getUser().getId().equals(currentUserId)) {
                throw new UnauthorizedException("Refresh token does not belong to the authenticated user");
            }
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.email()).ifPresent(user -> {
            String rawToken = jwtService.generateOpaqueToken();
            VerificationToken vt = new VerificationToken();
            vt.setUser(user);
            vt.setTokenHash(jwtService.hashToken(rawToken));
            vt.setType(VerificationTokenType.PASSWORD_RESET);
            vt.setExpiresAt(Instant.now().plus(PASSWORD_RESET_TTL_MINUTES, ChronoUnit.MINUTES));
            verificationTokenRepository.save(vt);
            mailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), rawToken);
        });
        // Deliberately does not reveal whether the email exists — generic response at the controller.
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String hash = jwtService.hashToken(request.token());
        VerificationToken vt = verificationTokenRepository.findByTokenHashAndType(hash, VerificationTokenType.PASSWORD_RESET)
                .orElseThrow(() -> new UnauthorizedException("Reset token invalid or expired"));
        if (!vt.isValid()) {
            throw new UnauthorizedException("Reset token invalid or expired");
        }
        User user = vt.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        vt.setUsedAt(Instant.now());
        verificationTokenRepository.save(vt);
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        String hash = jwtService.hashToken(request.token());
        VerificationToken vt = verificationTokenRepository.findByTokenHashAndType(hash, VerificationTokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new UnauthorizedException("Verification token invalid or expired"));
        if (!vt.isValid()) {
            throw new UnauthorizedException("Verification token invalid or expired");
        }
        User user = vt.getUser();
        user.setEmailVerifiedAt(Instant.now());
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
        vt.setUsedAt(Instant.now());
        verificationTokenRepository.save(vt);
    }

    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        userRepository.findByEmailIgnoreCase(request.email())
                .filter(u -> u.getStatus() == UserStatus.PENDING_VERIFICATION)
                .ifPresent(this::issueVerificationToken);
    }

    /**
     * Verifies the provider identity token server-side and issues AgriVerse's
     * own JWT pair, creating the account on first login. Provider token
     * verification (Google/GitHub public-key validation) is an external
     * integration seam — wire in the provider SDK here.
     */
    @Transactional
    public SocialLoginResponse socialLogin(SocialLoginRequest request) {
        SocialProviderIdentity identity = verifyProviderToken(request.provider(), request.providerToken());

        boolean isNewUser = false;
        User user = userRepository.findByEmailIgnoreCase(identity.email()).orElse(null);
        if (user == null) {
            Role readerRole = roleRepository.findByName(Role.READER)
                    .orElseThrow(() -> new IllegalStateException("READER role missing — check RBAC seed data"));
            user = new User();
            user.setFullName(identity.fullName());
            user.setEmail(identity.email().toLowerCase());
            user.setRole(readerRole);
            user.setStatus(UserStatus.ACTIVE);
            user.setEmailVerifiedAt(Instant.now());
            userRepository.save(user);
            isNewUser = true;
        }
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = issueRefreshToken(user);
        return new SocialLoginResponse(accessToken, refreshToken, isNewUser);
    }

    private record SocialProviderIdentity(String email, String fullName) {
    }

    private SocialProviderIdentity verifyProviderToken(String provider, String providerToken) {
        if (providerToken == null || providerToken.isBlank()) {
            throw new UnauthorizedException("Provider token invalid or expired");
        }
        // Placeholder for the real Google/GitHub token-verification call.
        // In production this exchanges providerToken for a verified profile
        // via the provider's userinfo/public-key endpoint.
        throw new BadRequestException("Social login provider '" + provider + "' is not yet configured on this server");
    }

    private void issueVerificationToken(User user) {
        String rawToken = jwtService.generateOpaqueToken();
        VerificationToken vt = new VerificationToken();
        vt.setUser(user);
        vt.setTokenHash(jwtService.hashToken(rawToken));
        vt.setType(VerificationTokenType.EMAIL_VERIFICATION);
        vt.setExpiresAt(Instant.now().plus(EMAIL_VERIFICATION_TTL_HOURS, ChronoUnit.HOURS));
        verificationTokenRepository.save(vt);
        mailService.sendVerificationEmail(user.getEmail(), user.getFullName(), rawToken);
    }

    private String issueRefreshToken(User user) {
        String rawToken = jwtService.generateOpaqueToken();
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(jwtService.hashToken(rawToken));
        token.setExpiresAt(Instant.now().plusSeconds(jwtService.refreshTokenTtlSeconds()));
        refreshTokenRepository.save(token);
        return rawToken;
    }

    private String sanitize(String input) {
        // Strips HTML/script tags per the register endpoint's validation rule.
        return input.replaceAll("<[^>]*>", "").trim();
    }
}
