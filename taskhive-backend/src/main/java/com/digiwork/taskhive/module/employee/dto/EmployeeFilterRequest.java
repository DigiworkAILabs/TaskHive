package com.digiwork.taskhive.module.employee.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeFilterRequest {
    private String name;
    private String email;
    private String department;
    private String status;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 10;
    private String sortBy;
    @Builder.Default
    private String sortDir = "asc";
}
