package com.digiwork.taskhive.module.employee.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class EmployeeActivatedEvent extends ApplicationEvent {
    private final UUID employeeId;
    private final UUID activatedBy;

    public EmployeeActivatedEvent(Object source, UUID employeeId, UUID activatedBy) {
        super(source);
        this.employeeId = employeeId;
        this.activatedBy = activatedBy;
    }
}
