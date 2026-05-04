package com.digiwork.taskhive.module.employee.service;

import com.digiwork.taskhive.common.exception.BusinessException;
import com.digiwork.taskhive.common.storage.StorageService;
import com.digiwork.taskhive.module.employee.exception.EmployeeNotFoundException;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfilePhotoService {

    @org.springframework.beans.factory.annotation.Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    private final EmployeeRepository employeeRepository;
    private final StorageService storageService;

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp");
    private static final String PHOTO_DIRECTORY = "employees/photos";

    @Transactional
    public String uploadPhoto(UUID employeeId, MultipartFile file) {
        // Validate file
        validateFile(file);
        String safeExtension = com.digiwork.taskhive.common.util.FileValidationUtil.getSafeExtension(file, ALLOWED_CONTENT_TYPES);

        Employee employee = employeeRepository.findByIdAndIsDeletedFalse(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));

        try {
            // Delete old photo if exists
            if (employee.getPhotoUrl() != null) {
                storageService.delete(employee.getPhotoUrl());
            }

            // Store new photo using employeeId as filename
            String storedPath = storageService.store(file, PHOTO_DIRECTORY, employeeId.toString(), safeExtension);

            // Update employee record
            employee.setPhotoUrl(storedPath);
            employeeRepository.save(employee);

            String fullUrl = storageService.getUrl(storedPath);
            log.info("Profile photo uploaded for employee: {}", employeeId);
            return fullUrl;

        } catch (IOException e) {
            log.error("Failed to upload photo for employee: {}", employeeId, e);
            throw new BusinessException("Failed to upload profile photo");
        }
    }

    public String getPhotoUrl(UUID employeeId) {
        Employee employee = employeeRepository.findByIdAndIsDeletedFalse(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));

        return employee.getPhotoUrl() != null
                ? backendUrl + "/api/v1/employees/" + employeeId + "/photo/download"
                : null;
    }

    public Resource loadPhotoAsResource(UUID employeeId) {
        Employee employee = employeeRepository.findByIdAndIsDeletedFalse(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));

        if (employee.getPhotoUrl() == null) {
            throw new BusinessException("Employee does not have a profile photo");
        }

        try {
            return storageService.loadAsResource(employee.getPhotoUrl());
        } catch (IOException e) {
            log.error("Failed to load photo for employee: {}", employeeId, e);
            throw new BusinessException("Could not read profile photo");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File size exceeds maximum limit of 5MB");
        }

        com.digiwork.taskhive.common.util.FileValidationUtil.validateContentType(file, ALLOWED_CONTENT_TYPES);
    }
}
