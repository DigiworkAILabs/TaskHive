package com.digiwork.taskhive.common.config;

import com.digiwork.taskhive.common.constants.CookieConstants;
import com.digiwork.taskhive.module.auth.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;

/**
 * Intercepts the SockJS HTTP handshake request to authenticate the connecting user.
 *
 * <p>SockJS establishes the WebSocket channel through a standard HTTP upgrade request,
 * which means the browser automatically includes all applicable cookies — including the
 * HttpOnly {@code accessToken} cookie. This interceptor reads and validates the JWT at
 * that point, then stores the resolved {@code userId} and {@code role} in the WebSocket
 * session attribute map so they are available to the downstream
 * {@link WebSocketChannelInterceptor} on every subsequent STOMP frame.</p>
 *
 * <p>If the token is absent or invalid the handshake is still allowed to proceed
 * (returning {@code true}), but no auth attributes are populated. The
 * {@link WebSocketChannelInterceptor} then rejects the STOMP {@code CONNECT} frame,
 * cleanly closing the session with an appropriate error rather than at the raw TCP level.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            Cookie[] cookies = httpRequest.getCookies();

            if (cookies != null) {
                Arrays.stream(cookies)
                        .filter(c -> CookieConstants.ACCESS_TOKEN_COOKIE.equals(c.getName()))
                        .findFirst()
                        .ifPresentOrElse(
                                cookie -> {
                                    String token = cookie.getValue();
                                    if (jwtTokenProvider.validateToken(token)) {
                                        String userId = jwtTokenProvider.getUserIdFromToken(token).toString();
                                        String role   = jwtTokenProvider.getRoleFromToken(token);
                                        attributes.put("wsUserId", userId);
                                        attributes.put("wsRole",   role);
                                        log.debug("[WS-Handshake] Authenticated userId={} role={}", userId, role);
                                    } else {
                                        log.warn("[WS-Handshake] JWT present but invalid — rejecting auth attributes");
                                    }
                                },
                                () -> log.debug("[WS-Handshake] No accessToken cookie — session will be unauthenticated")
                        );
            }
        }

        // Always return true: raw handshake must succeed for SockJS to work.
        // The STOMP CONNECT interceptor enforces authentication at the protocol level.
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }
}
