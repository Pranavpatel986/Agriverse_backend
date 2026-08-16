package com.agriverse.api.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Reads the Authorization: Bearer header, validates the JWT, and populates the SecurityContext. */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.isValid(token)) {
                Claims claims = jwtService.parseClaims(token);
                String email = claims.get("email", String.class);
                try {
                    var userDetails = userDetailsService.loadUserByUsername(email);
                    if (userDetails.isEnabled()) {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (UsernameNotFoundException e) {
                    // Expected, routine case: token's subject no longer exists (deleted
                    // account). Leave the request unauthenticated; downstream 401 applies.
                    // No logging — this isn't a bug, it happens on every stale token.
                } catch (Exception e) {
                    // Anything else (DB/connection errors, a bug in UserDetailsService,
                    // lazy-loading issues, etc.) is NOT the same as "not authenticated" —
                    // it's a real failure that was previously silently swallowed here,
                    // making it indistinguishable from a routine 401 with zero trace in
                    // the logs. Log it loudly; still leave the request unauthenticated
                    // rather than throwing, since failing open here would be worse.
                    log.error("Unexpected error authenticating request to {}: {}",
                            request.getRequestURI(), e.toString(), e);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
