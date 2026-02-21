package com.digiwork.taskhive.module.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {

    private String firstName;

    private String lastName;

    private String phone;

    private String department;

    private String designation;

    private LocalDate joinDate;

    private UUID managerId;
}
