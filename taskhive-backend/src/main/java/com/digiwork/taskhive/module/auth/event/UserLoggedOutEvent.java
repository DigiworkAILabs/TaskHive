package com.digiwork.taskhive.module.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class UserLoggedOutEvent extends ApplicationEvent {
    private final UUID userId;

    public UserLoggedOutEvent(Object source, UUID userId) {
        super(source);
        this.userId = userId;
    }
}
