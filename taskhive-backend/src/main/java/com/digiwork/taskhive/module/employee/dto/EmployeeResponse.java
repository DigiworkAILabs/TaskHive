package com.digiwork.taskhive.module.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private String designation;
    private String managerId;
    private String managerName;
    private LocalDate joinDate;
    private String photoUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
