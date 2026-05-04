package com.digiwork.taskhive.module.auth.security;

import com.digiwork.taskhive.common.constants.CookieConstants;
import com.digiwork.taskhive.common.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = cookieUtil.extractCookieValue(request, CookieConstants.ACCESS_TOKEN_COOKIE);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                    log.warn("Blocked ghost session for user: {} (enabled={}, nonLocked={})",
                            email, userDetails.isEnabled(), userDetails.isAccountNonLocked());
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                if (userDetails instanceof CustomUserDetails customUserDetails) {
                    Long tokenVersion = jwtTokenProvider.getTokenVersionFromToken(token);
                    if (tokenVersion == null || !tokenVersion.equals(customUserDetails.getTokenVersion())) {
                        log.warn("Invalid token version for user: {}. Token version: {}, DB version: {}",
                                email, tokenVersion, customUserDetails.getTokenVersion());
                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.debug("Could not set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
