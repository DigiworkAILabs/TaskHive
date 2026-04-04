package com.digiwork.taskhive.common.util;

import com.digiwork.taskhive.common.constants.CookieConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookieUtilTest {

    @InjectMocks
    private CookieUtil cookieUtil;

    private HttpServletResponse response;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        response = mock(HttpServletResponse.class);
        request = mock(HttpServletRequest.class);
        
        // Inject @Value fields via Reflection
        ReflectionTestUtils.setField(cookieUtil, "secure", true);
        ReflectionTestUtils.setField(cookieUtil, "sameSite", "Strict");
        ReflectionTestUtils.setField(cookieUtil, "domain", "localhost");
        ReflectionTestUtils.setField(cookieUtil, "refreshTokenPath", "/api/v1/auth/refresh");
        ReflectionTestUtils.setField(cookieUtil, "defaultPath", "/");
    }

    @Test
    @DisplayName("should add Access Token cookie")
    void shouldAddAccessTokenCookie() {
        String token = "test-access-token";
        cookieUtil.addAccessTokenCookie(response, token);

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());
        
        String cookieHeader = headerCaptor.getValue();
        assertThat(cookieHeader)
                .contains(CookieConstants.ACCESS_TOKEN_COOKIE + "=" + token)
                .contains("HttpOnly")
                .contains("Secure")
                .contains("SameSite=Strict")
                .contains("Path=/");
    }

    @Test
    @DisplayName("should add Refresh Token cookie")
    void shouldAddRefreshTokenCookie() {
        String token = "test-refresh-token";
        cookieUtil.addRefreshTokenCookie(response, token);

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());
        
        String cookieHeader = headerCaptor.getValue();
        assertThat(cookieHeader)
                .contains(CookieConstants.REFRESH_TOKEN_COOKIE + "=" + token)
                .contains("Path=/api/v1/auth/refresh");
    }

    @Test
    @DisplayName("should add User Role cookie (Non-HttpOnly)")
    void shouldAddUserRoleCookie() {
        String role = "ADMIN";
        cookieUtil.addUserRoleCookie(response, role);

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());
        
        String cookieHeader = headerCaptor.getValue();
        assertThat(cookieHeader)
                .contains(CookieConstants.USER_ROLE_COOKIE + "=" + role)
                .doesNotContain("HttpOnly");
    }

    @Test
    @DisplayName("should clear all auth cookies")
    void shouldClearAllAuthCookies() {
        cookieUtil.clearAllAuthCookies(response);

        verify(response, times(3)).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    @DisplayName("should extract cookie value")
    void shouldExtractCookieValue() {
        Cookie[] cookies = {
            new Cookie("other", "val"),
            new Cookie(CookieConstants.ACCESS_TOKEN_COOKIE, "target-val")
        };
        when(request.getCookies()).thenReturn(cookies);

        String result = cookieUtil.extractCookieValue(request, CookieConstants.ACCESS_TOKEN_COOKIE);

        assertThat(result).isEqualTo("target-val");
    }

    @Test
    @DisplayName("should return null when cookie is missing")
    void shouldReturnNullWhenCookieMissing() {
        when(request.getCookies()).thenReturn(null);
        String result = cookieUtil.extractCookieValue(request, "missing");
        assertThat(result).isNull();

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("a", "b")});
        assertThat(cookieUtil.extractCookieValue(request, "missing")).isNull();
    }
}
