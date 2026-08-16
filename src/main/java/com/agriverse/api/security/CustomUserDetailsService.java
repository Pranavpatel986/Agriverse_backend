package com.agriverse.api.security;

import com.agriverse.api.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * @Transactional is load-bearing here, not decorative: User.role is
     * FetchType.LAZY. findByEmailIgnoreCase()'s own transaction (Spring
     * Data wraps each repository method individually) closes the moment
     * it returns, so without this annotation .map(UserPrincipal::new) —
     * which touches user.getRole() in UserPrincipal's constructor — runs
     * with no open Hibernate session and throws LazyInitializationException
     * on every single call. That exception used to be silently swallowed
     * by JwtAuthenticationFilter's catch-all, making every authenticated
     * request fail as a plain, untraceable 401 regardless of token
     * validity. Keeping the session open for the whole method (including
     * the .map() chain called within it) fixes that at the source.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("No user with email " + email));
    }
}
