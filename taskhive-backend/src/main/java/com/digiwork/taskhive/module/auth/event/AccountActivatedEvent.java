package com.digiwork.taskhive.module.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class AccountActivatedEvent extends ApplicationEvent {
    private final UUID userId;
    private final String email;

    public AccountActivatedEvent(Object source, UUID userId, String email) {
        super(source);
        this.userId = userId;
        this.email = email;
    }
}
