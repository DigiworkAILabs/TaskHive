package com.digiwork.taskhive.module.employee.service;

import com.digiwork.taskhive.common.exception.BusinessException;
import com.digiwork.taskhive.common.storage.StorageService;
import com.digiwork.taskhive.module.employee.exception.EmployeeNotFoundException;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final EmployeeRepository employeeRepository;
    private final StorageService storageService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp");
    private static final String PHOTO_DIRECTORY = "employees/photos";

    @Transactional
    public String uploadPhoto(UUID employeeId, MultipartFile file) {
        // Validate file
        validateFile(file);

        Employee employee = employeeRepository.findByIdAndIsDeletedFalse(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));

        try {
            // Delete old photo if exists
            if (employee.getPhotoUrl() != null) {
                storageService.delete(employee.getPhotoUrl());
            }

            // Store new photo using employeeId as filename
            String storedPath = storageService.store(file, PHOTO_DIRECTORY, employeeId.toString());

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
                ? storageService.getUrl(employee.getPhotoUrl())
                : null;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File size exceeds maximum limit of 5MB");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException("Only JPG, PNG, and WebP files are allowed");
        }
    }
}
