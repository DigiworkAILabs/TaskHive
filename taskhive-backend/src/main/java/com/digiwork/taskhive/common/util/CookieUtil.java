package com.digiwork.taskhive.common.util;

import com.digiwork.taskhive.common.constants.CookieConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    private static final String SET_COOKIE_HEADER = "Set-Cookie";

    @Value("${app.cookie.secure}")
    private boolean secure;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    @Value("${app.cookie.domain}")
    private String domain;

    @Value("${app.cookie.refresh-token-path:/api/v1/auth/refresh}")
    private String refreshTokenPath;

    @Value("${app.cookie.default-path:/}")
    private String defaultPath;

    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(CookieConstants.ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(defaultPath)
                .maxAge(CookieConstants.ACCESS_TOKEN_MAX_AGE)
                .build();
        response.addHeader(SET_COOKIE_HEADER, cookie.toString());
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(CookieConstants.REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(refreshTokenPath)
                .maxAge(CookieConstants.REFRESH_TOKEN_MAX_AGE)
                .build();
        response.addHeader(SET_COOKIE_HEADER, cookie.toString());
    }

    public void addUserRoleCookie(HttpServletResponse response, String role) {
        ResponseCookie cookie = ResponseCookie.from(CookieConstants.USER_ROLE_COOKIE, role)
                .httpOnly(false) // Non-HttpOnly — readable by Next.js middleware
                .secure(secure)
                .sameSite(sameSite)
                .path(defaultPath)
                .maxAge(CookieConstants.USER_ROLE_MAX_AGE)
                .build();
        response.addHeader(SET_COOKIE_HEADER, cookie.toString());
    }

    public void clearAllAuthCookies(HttpServletResponse response) {
        clearCookie(response, CookieConstants.ACCESS_TOKEN_COOKIE, defaultPath);
        clearCookie(response, CookieConstants.REFRESH_TOKEN_COOKIE, refreshTokenPath);
        clearCookie(response, CookieConstants.USER_ROLE_COOKIE, defaultPath);
    }

    private void clearCookie(HttpServletResponse response, String name, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(0)
                .build();
        response.addHeader(SET_COOKIE_HEADER, cookie.toString());
    }

    public String extractCookieValue(jakarta.servlet.http.HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
