package com.digiwork.taskhive.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /** Validates the JWT cookie during the SockJS HTTP handshake. */
    private final WebSocketAuthHandshakeInterceptor handshakeInterceptor;

    /** Enforces authentication on STOMP CONNECT and ownership on STOMP SUBSCRIBE. */
    private final WebSocketChannelInterceptor channelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendUrl)
                .addInterceptors(handshakeInterceptor)   // ← JWT extraction at HTTP level
                .withSockJS();
    }

    /**
     * Registers the channel interceptor so every inbound STOMP frame is
     * vetted before it reaches the broker or any @MessageMapping handler.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor);
    }
}
