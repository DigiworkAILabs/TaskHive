package com.digiwork.taskhive.module.employee.mapper;

import com.digiwork.taskhive.common.storage.StorageService;
import com.digiwork.taskhive.module.employee.dto.EmployeeListResponse;
import com.digiwork.taskhive.module.employee.dto.EmployeeResponse;
import com.digiwork.taskhive.module.employee.model.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeMapper {

    private final StorageService storageService;

    public EmployeeResponse toEmployeeResponse(Employee employee, String managerName) {
        return EmployeeResponse.builder()
                .id(employee.getId().toString())
                .userId(employee.getUserId().toString())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .managerId(employee.getManagerId() != null ? employee.getManagerId().toString() : null)
                .managerName(managerName)
                .joinDate(employee.getJoinDate())
                .photoUrl(storageService.getUrl(employee.getPhotoUrl()))
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    public EmployeeListResponse toEmployeeListResponse(Employee employee) {
        return EmployeeListResponse.builder()
                .id(employee.getId().toString())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .status(employee.getStatus())
                .photoUrl(storageService.getUrl(employee.getPhotoUrl()))
                .joinDate(employee.getJoinDate())
                .build();
    }
}
