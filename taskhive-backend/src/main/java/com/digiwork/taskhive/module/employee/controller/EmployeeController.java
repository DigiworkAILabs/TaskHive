package com.digiwork.taskhive.module.employee.controller;

import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.employee.dto.*;
import com.digiwork.taskhive.module.employee.service.EmployeeSearchService;
import com.digiwork.taskhive.module.employee.service.EmployeeService;
import com.digiwork.taskhive.module.employee.service.ProfilePhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeSearchService employeeSearchService;
    private final ProfilePhotoService profilePhotoService;

    // ─── POST /api/v1/employees ───────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse employee = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", employee));
    }

    // ─── GET /api/v1/employees ────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeListResponse>>> listEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<EmployeeListResponse> employees = employeeService.listEmployees(
                name, email, department, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }

    // ─── GET /api/v1/employees/search?query= ─────────────────────────────────
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeListResponse>>> searchEmployees(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<EmployeeListResponse> results = employeeSearchService.searchEmployees(query, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search results", results));
    }

    // ─── GET /api/v1/employees/{id} ───────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable UUID id) {
        EmployeeResponse employee = employeeService.getEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", employee));
    }

    // ─── PUT /api/v1/employees/{id} ───────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable UUID id,
            @RequestBody UpdateEmployeeRequest request) {
        EmployeeResponse employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", employee));
    }

    // ─── DELETE /api/v1/employees/{id} ────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable UUID id) {
        employeeService.softDeleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully"));
    }

    // ─── PATCH /api/v1/employees/{id}/activate ────────────────────────────────
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> activateEmployee(@PathVariable UUID id) {
        EmployeeResponse employee = employeeService.activateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee activated successfully", employee));
    }

    // ─── PATCH /api/v1/employees/{id}/deactivate ──────────────────────────────
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> deactivateEmployee(@PathVariable UUID id) {
        EmployeeResponse employee = employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated successfully", employee));
    }

    // ─── POST /api/v1/employees/{id}/photo ────────────────────────────────────
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadPhoto(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        String photoUrl = profilePhotoService.uploadPhoto(id, file);
        return ResponseEntity.ok(ApiResponse.success("Profile photo uploaded successfully", photoUrl));
    }

    // ─── GET /api/v1/employees/{id}/photo ─────────────────────────────────────
    @GetMapping("/{id}/photo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> getPhoto(@PathVariable UUID id) {
        String photoUrl = profilePhotoService.getPhotoUrl(id);
        return ResponseEntity.ok(ApiResponse.success("Photo URL retrieved", photoUrl));
    }
}
