package com.digiwork.taskhive.module.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSearchRequest {

    private String query;
    private String name;
    private String email;
    private String department;
    private String designation;
}
