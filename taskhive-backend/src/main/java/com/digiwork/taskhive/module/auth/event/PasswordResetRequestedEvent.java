package com.digiwork.taskhive.module.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class PasswordResetRequestedEvent extends ApplicationEvent {
    private final UUID userId;
    private final String email;
    private final String resetToken;

    public PasswordResetRequestedEvent(Object source, UUID userId, String email, String resetToken) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.resetToken = resetToken;
    }
}
