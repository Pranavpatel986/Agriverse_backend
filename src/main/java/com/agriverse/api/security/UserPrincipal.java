package com.agriverse.api.security;

import com.agriverse.api.identity.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Spring Security principal wrapping the domain User, exposing the role as a ROLE_ authority. */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final UUID publicId;
    private final String email;
    private final String passwordHash;
    private final String role;
    private final boolean enabled;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.publicId = user.getPublicId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole().getName();
        this.enabled = user.getStatus() == com.agriverse.api.identity.entity.UserStatus.ACTIVE;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
