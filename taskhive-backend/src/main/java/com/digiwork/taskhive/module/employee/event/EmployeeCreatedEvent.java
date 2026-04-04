package com.digiwork.taskhive.module.employee.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class EmployeeCreatedEvent extends ApplicationEvent {
    private final UUID employeeId;
    private final UUID userId;
    private final String email;
    private final String firstName;
    private final String activationToken;
    private final UUID createdBy;  // admin/actor who created the employee

    public EmployeeCreatedEvent(Object source, UUID employeeId, UUID userId, String email,
            String firstName, String activationToken, UUID createdBy) {
        super(source);
        this.employeeId = employeeId;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.activationToken = activationToken;
        this.createdBy = createdBy;
    }
}
