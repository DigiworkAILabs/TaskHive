package com.digiwork.taskhive.module.employee.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.employee.dto.EmployeeListResponse;
import com.digiwork.taskhive.module.employee.mapper.EmployeeMapper;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeSearchService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Transactional(readOnly = true)
    public PageResponse<EmployeeListResponse> searchEmployees(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "firstName"));
        Page<Employee> employeePage = employeeRepository.searchEmployees(query, pageable);

        return PageResponse.<EmployeeListResponse>builder()
                .content(employeePage.getContent().stream()
                        .map(employeeMapper::toEmployeeListResponse)
                        .toList())
                .page(employeePage.getNumber())
                .size(employeePage.getSize())
                .totalElements(employeePage.getTotalElements())
                .totalPages(employeePage.getTotalPages())
                .last(employeePage.isLast())
                .build();
    }
}
