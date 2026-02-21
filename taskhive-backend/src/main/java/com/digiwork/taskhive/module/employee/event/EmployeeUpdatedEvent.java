package com.digiwork.taskhive.module.employee.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class EmployeeUpdatedEvent extends ApplicationEvent {
    private final UUID employeeId;
    private final UUID updatedBy;

    public EmployeeUpdatedEvent(Object source, UUID employeeId, UUID updatedBy) {
        super(source);
        this.employeeId = employeeId;
        this.updatedBy = updatedBy;
    }
}
