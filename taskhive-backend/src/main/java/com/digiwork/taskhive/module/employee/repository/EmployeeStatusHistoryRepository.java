package com.digiwork.taskhive.module.employee.repository;

import com.digiwork.taskhive.module.employee.model.EmployeeStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeStatusHistoryRepository extends JpaRepository<EmployeeStatusHistory, UUID> {

    List<EmployeeStatusHistory> findByEmployeeIdOrderByChangedAtDesc(UUID employeeId);
    @Transactional
    void deleteByEmployeeId(UUID employeeId);
}
