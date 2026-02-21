package com.digiwork.taskhive.module.employee.repository;

import com.digiwork.taskhive.module.employee.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

        Optional<Employee> findByIdAndIsDeletedFalse(UUID id);

        Optional<Employee> findByUserIdAndIsDeletedFalse(UUID userId);

        Optional<Employee> findByEmailAndIsDeletedFalse(String email);

        boolean existsByEmail(String email);

        boolean existsByUserId(UUID userId);

        @Query("SELECT e FROM Employee e WHERE e.isDeleted = false " +
                        "AND (COALESCE(:name, '') = '' OR LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) "
                        +
                        "AND (COALESCE(:email, '') = '' OR LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
                        "AND (COALESCE(:department, '') = '' OR LOWER(e.department) = LOWER(:department)) " +
                        "AND (COALESCE(:status, '') = '' OR e.status = :status)")
        Page<Employee> findAllWithFilters(
                        @Param("name") String name,
                        @Param("email") String email,
                        @Param("department") String department,
                        @Param("status") String status,
                        Pageable pageable);

        @Query("SELECT e FROM Employee e WHERE e.isDeleted = false " +
                        "AND (LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(e.department) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(e.designation) LIKE LOWER(CONCAT('%', :query, '%')))")
        Page<Employee> searchEmployees(@Param("query") String query, Pageable pageable);
}
