package com.digiwork.taskhive.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * STOMP-level security enforcer for the WebSocket message channel.
 *
 * <p>This interceptor runs on every inbound STOMP frame and enforces two rules:</p>
 *
 * <ol>
 *   <li><b>CONNECT</b> — Reads the {@code wsUserId} / {@code wsRole} attributes that
 *       {@link WebSocketAuthHandshakeInterceptor} deposited during the HTTP handshake.
 *       If they are absent (meaning no valid JWT was presented) the connection is rejected
 *       immediately by throwing a {@link MessagingException}, which Spring converts into
 *       a STOMP {@code ERROR} frame and closes the session.  When auth attributes are
 *       present a {@link UsernamePasswordAuthenticationToken} is installed as the STOMP
 *       session's {@code simpUser} principal, making the identity available to all
 *       subsequent frames in the same session.</li>
 *
 *   <li><b>SUBSCRIBE</b> — Verifies that the destination is either a shared/system topic
 *       or a personal channel that belongs to the authenticated user.
 *       Concretely, a subscription to {@code /topic/notifications/{id}} is only permitted
 *       when {@code id} equals the authenticated user's UUID, preventing cross-user
 *       data eavesdropping (the "wiretap" attack described in the security audit).</li>
 * </ol>
 */
@Slf4j
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    /** Session attribute keys written by {@link WebSocketAuthHandshakeInterceptor}. */
    private static final String ATTR_USER_ID = "wsUserId";
    private static final String ATTR_ROLE    = "wsRole";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();
        if (command == null)  return message;

        switch (command) {

            case CONNECT -> handleConnect(accessor);

            case SUBSCRIBE -> handleSubscribe(accessor);

            default -> { /* SEND, DISCONNECT, etc. — no additional checks needed */ }
        }

        return message;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reject unauthenticated CONNECT frames and set the {@code simpUser} principal
     * for authenticated sessions.
     */
    private void handleConnect(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes == null
                || !sessionAttributes.containsKey(ATTR_USER_ID)
                || !sessionAttributes.containsKey(ATTR_ROLE)) {

            log.warn("[WS-Security] CONNECT rejected — session has no auth attributes " +
                     "(missing or invalid JWT cookie during handshake). sessionId={}",
                     accessor.getSessionId());

            throw new MessagingException(
                    "WebSocket connection rejected: authentication required. " +
                    "Ensure a valid accessToken cookie is present.");
        }

        String userId = (String) sessionAttributes.get(ATTR_USER_ID);
        String role   = (String) sessionAttributes.get(ATTR_ROLE);

        // Install the authenticated principal on the STOMP session.
        // Spring will attach this to every subsequent frame in this session.
        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));

        accessor.setUser(principal);

        log.info("[WS-Security] CONNECT accepted — userId={} role={} sessionId={}",
                 userId, role, accessor.getSessionId());
    }

    /**
     * Verify that the subscribing user owns the requested personal topic.
     * Shared broadcast topics (if any) are unrestricted.
     */
    private void handleSubscribe(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        String    destination = accessor.getDestination();

        // If somehow a SUBSCRIBE arrives without a principal (shouldn't happen after CONNECT
        // enforcement, but be defensive), reject it.
        if (principal == null) {
            log.warn("[WS-Security] SUBSCRIBE rejected — no authenticated principal. " +
                     "destination={} sessionId={}", destination, accessor.getSessionId());
            throw new MessagingException(
                    "WebSocket subscription rejected: authentication required.");
        }

        // Enforce ownership of personal notification topics
        if (destination != null && destination.startsWith("/topic/notifications/")) {
            String topicUserId         = destination.substring("/topic/notifications/".length());
            String authenticatedUserId = principal.getName();

            if (!authenticatedUserId.equals(topicUserId)) {
                log.warn("[WS-Security] SUBSCRIBE denied — cross-user eavesdrop attempt! " +
                         "authenticatedUserId={} attemptedTopic={} sessionId={}",
                         authenticatedUserId, destination, accessor.getSessionId());
                throw new MessagingException(
                        "WebSocket subscription denied: you may only subscribe to your own notification topic.");
            }
        }

        log.debug("[WS-Security] SUBSCRIBE allowed — userId={} destination={} sessionId={}",
                  principal.getName(), destination, accessor.getSessionId());
    }
}
