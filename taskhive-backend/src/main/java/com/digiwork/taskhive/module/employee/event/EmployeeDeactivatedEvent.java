package com.digiwork.taskhive.module.employee.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class EmployeeDeactivatedEvent extends ApplicationEvent {
    private final UUID employeeId;
    private final UUID deactivatedBy;

    public EmployeeDeactivatedEvent(Object source, UUID employeeId, UUID deactivatedBy) {
        super(source);
        this.employeeId = employeeId;
        this.deactivatedBy = deactivatedBy;
    }
}
