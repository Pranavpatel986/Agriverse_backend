package com.agriverse.api.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Closes the roadmap's "Admin 'create user' / invite flow" gap: previously
 * the only way an account came to exist was self-registration via
 * /auth/register. This is the deliberately simple version — admin sets an
 * initial password directly and the account is created ACTIVE (skips
 * email verification, since an admin vouching for the account is a
 * reasonable substitute for self-verification). A proper email-invite flow
 * (temp token, "set your password" page) is a real future improvement, not
 * done here to keep this addition scoped to "an admin can get a teammate a
 * working login," which is the actual gap.
 */
public record CreateUserRequest(
        @NotBlank @Size(min = 2, max = 150) String fullName,
        @NotBlank @Size(max = 255) String email,
        @NotBlank @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                message = "must be at least 8 characters and include a letter and a number")
        String password,
        @NotBlank String role) {
}
