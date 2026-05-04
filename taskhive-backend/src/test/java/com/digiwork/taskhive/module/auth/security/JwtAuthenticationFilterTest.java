package com.digiwork.taskhive.module.auth.security;

import com.digiwork.taskhive.common.constants.CookieConstants;
import com.digiwork.taskhive.common.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private CookieUtil cookieUtil;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("should set authentication when valid token in cookie")
    void shouldSetAuthentication_whenValidTokenInCookie() throws Exception {
        // given
        when(cookieUtil.extractCookieValue(request, CookieConstants.ACCESS_TOKEN_COOKIE))
                .thenReturn("valid-jwt-token");
        when(jwtTokenProvider.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken("valid-jwt-token")).thenReturn("user@test.com");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getAuthorities()).thenReturn(java.util.List.of());
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(true);
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should not set authentication when no cookie present")
    void shouldNotSetAuthentication_whenNoCookie() throws Exception {
        when(cookieUtil.extractCookieValue(request, CookieConstants.ACCESS_TOKEN_COOKIE))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should not set authentication when token is invalid")
    void shouldNotSetAuthentication_whenInvalidToken() throws Exception {
        when(cookieUtil.extractCookieValue(request, CookieConstants.ACCESS_TOKEN_COOKIE))
                .thenReturn("invalid-token");
        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should continue filter chain even when an exception occurs")
    void shouldContinueFilterChain_evenWhenExceptionOccurs() throws Exception {
        when(cookieUtil.extractCookieValue(request, CookieConstants.ACCESS_TOKEN_COOKIE))
                .thenThrow(new RuntimeException("Cookie parsing error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should not set authentication when account is disabled")
    void shouldNotSetAuthentication_whenAccountDisabled() throws Exception {
        when(cookieUtil.extractCookieValue(request, CookieConstants.ACCESS_TOKEN_COOKIE))
                .thenReturn("valid-jwt-token");
        when(jwtTokenProvider.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken("valid-jwt-token")).thenReturn("user@test.com");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.isEnabled()).thenReturn(false);
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should not set authentication when account is locked")
    void shouldNotSetAuthentication_whenAccountLocked() throws Exception {
        when(cookieUtil.extractCookieValue(request, CookieConstants.ACCESS_TOKEN_COOKIE))
                .thenReturn("valid-jwt-token");
        when(jwtTokenProvider.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken("valid-jwt-token")).thenReturn("user@test.com");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(false);
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
