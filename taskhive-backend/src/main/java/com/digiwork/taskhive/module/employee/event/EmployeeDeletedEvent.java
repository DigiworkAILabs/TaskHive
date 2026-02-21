package com.digiwork.taskhive.module.employee.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class EmployeeDeletedEvent extends ApplicationEvent {
    private final UUID employeeId;
    private final UUID deletedBy;

    public EmployeeDeletedEvent(Object source, UUID employeeId, UUID deletedBy) {
        super(source);
        this.employeeId = employeeId;
        this.deletedBy = deletedBy;
    }
}
