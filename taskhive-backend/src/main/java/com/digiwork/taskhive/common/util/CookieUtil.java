package com.digiwork.taskhive.common.util;

import com.digiwork.taskhive.common.constants.CookieConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${app.cookie.secure}")
    private boolean secure;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    @Value("${app.cookie.domain}")
    private String domain;

    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(CookieConstants.ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(CookieConstants.DEFAULT_PATH)
                .maxAge(CookieConstants.ACCESS_TOKEN_MAX_AGE)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(CookieConstants.REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(CookieConstants.REFRESH_TOKEN_PATH)
                .maxAge(CookieConstants.REFRESH_TOKEN_MAX_AGE)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void addUserRoleCookie(HttpServletResponse response, String role) {
        ResponseCookie cookie = ResponseCookie.from(CookieConstants.USER_ROLE_COOKIE, role)
                .httpOnly(false) // Non-HttpOnly — readable by Next.js middleware
                .secure(secure)
                .sameSite(sameSite)
                .path(CookieConstants.DEFAULT_PATH)
                .maxAge(CookieConstants.USER_ROLE_MAX_AGE)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearAllAuthCookies(HttpServletResponse response) {
        clearCookie(response, CookieConstants.ACCESS_TOKEN_COOKIE, CookieConstants.DEFAULT_PATH);
        clearCookie(response, CookieConstants.REFRESH_TOKEN_COOKIE, CookieConstants.REFRESH_TOKEN_PATH);
        clearCookie(response, CookieConstants.USER_ROLE_COOKIE, CookieConstants.DEFAULT_PATH);
    }

    private void clearCookie(HttpServletResponse response, String name, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
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
