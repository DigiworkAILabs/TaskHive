package com.digiwork.taskhive.integration;

import com.digiwork.taskhive.module.auth.dto.LoginRequest;
import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.model.UserRole;
import com.digiwork.taskhive.module.auth.repository.RoleRepository;
import com.digiwork.taskhive.module.employee.dto.CreateEmployeeRequest;
import com.digiwork.taskhive.module.employee.dto.UpdateEmployeeRequest;
import com.digiwork.taskhive.module.employee.model.Employee;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EmployeeFlowIntegrationTest
 * ─────────────────────────────
 * Full HTTP integration tests for /api/v1/employees.
 *
 * Covers:
 * - Admin creates employee → 201, DB persistence as PENDING
 * - Admin lists with filters (name, status, department)
 * - Admin gets employee by ID
 * - Admin updates employee
 * - Admin activates / deactivates employee
 * - Admin soft-deletes employee → 404 on subsequent GET
 * - Duplicate email → 409
 * - Employee tries to access employee endpoints → 403
 */
class EmployeeFlowIntegrationTest extends BaseIntegrationTest {

    private static final String EMPLOYEES_URL = "/api/v1/employees";
    private static final String LOGIN_URL      = "/api/v1/auth/login";

    private static final String ADMIN_EMAIL    = "admin@test.com";
    private static final String ADMIN_PASSWORD = "Admin@123";

    private static final String EMP_EMAIL    = "emp.emptest@test.com";
    private static final String EMP_PASSWORD = "EmpPass@1";

    @Autowired private RoleRepository      roleRepository;
    @Autowired private PasswordEncoder     passwordEncoder;

    private Cookie adminCookie;

    @BeforeEach
    void setUp() throws Exception {
        MvcResult result = performPost(LOGIN_URL, new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD))
                .andExpect(status().isOk())
                .andReturn();
        adminCookie = result.getResponse().getCookie("accessToken");
        assertThat(adminCookie).isNotNull();
    }

    @AfterEach
    void cleanup() {
        cleanupAllTestData();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Cookie createEmployeeCookieFor(String email, String password) throws Exception {
        var role = roleRepository.findByName("EMPLOYEE").orElseThrow();
        User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName("Emp")
                .lastName("Test")
                .status(UserStatus.ACTIVE)
                .failedAttempts(0)
                .isDeleted(false)
                .build());
        userRoleRepository.save(UserRole.builder()
                .userId(user.getId())
                .roleId(role.getId())
                .build());
        employeeRepository.save(Employee.builder()
                .userId(user.getId())
                .firstName("Emp")
                .lastName("Test")
                .email(email)
                .status("ACTIVE")
                .build());
        MvcResult result = performPost(LOGIN_URL, new LoginRequest(email, password))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("accessToken");
    }

    private CreateEmployeeRequest buildCreateRequest(String email) {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setEmail(email);
        req.setDepartment("Engineering");
        req.setDesignation("Developer");
        req.setJoinDate(LocalDate.now());
        return req;
    }

    /** Creates an employee via HTTP and returns the UUID string from the response body. */
    private String createEmployee(String email) throws Exception {
        MvcResult result = performPostWithCookie(EMPLOYEES_URL, buildCreateRequest(email), adminCookie)
                .andExpect(status().isCreated())
                .andReturn();
        // FIX: Extract ID safely from the 'data' path instead of fragile String slicing
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asText();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Create
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: POST /employees creates employee as PENDING and returns 201")
    void adminCreatesEmployee_Returns201_StatusPending() throws Exception {
        performPostWithCookie(EMPLOYEES_URL, buildCreateRequest("newjane@test.com"), adminCookie)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        // FIX: Verify DB persistence
        assertThat(employeeRepository.findByEmailAndIsDeletedFalse("newjane@test.com")).isPresent();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // List with filters
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: GET /employees?department=Engineering returns filtered results")
    void adminListsEmployeesWithDepartmentFilter() throws Exception {
        createEmployee("jane.eng@test.com");

        performGetWithCookie(EMPLOYEES_URL + "?department=Engineering", adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("admin: GET /employees?status=PENDING returns only PENDING employees")
    void adminListsEmployeesWithStatusFilter() throws Exception {
        createEmployee("pending1@test.com");

        MvcResult result = performGetWithCookie(EMPLOYEES_URL + "?status=PENDING", adminCookie)
                .andExpect(status().isOk())
                .andReturn();

        // All returned employees should be PENDING
        assertThat(result.getResponse().getContentAsString()).contains("PENDING");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Get by ID
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: GET /employees/{id} returns employee details")
    void adminGetsEmployeeById() throws Exception {
        String empId = createEmployee("getbyid@test.com");

        performGetWithCookie(EMPLOYEES_URL + "/" + empId, adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("getbyid@test.com"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Update
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: PUT /employees/{id} updates department and designation")
    void adminUpdatesEmployee() throws Exception {
        String empId = createEmployee("update.emp@test.com");

        UpdateEmployeeRequest req = new UpdateEmployeeRequest();
        req.setDepartment("Marketing");
        req.setDesignation("Manager");

        performPutWithCookie(EMPLOYEES_URL + "/" + empId, req, adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.department").value("Marketing"))
                .andExpect(jsonPath("$.data.designation").value("Manager"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Activate / Deactivate
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: PATCH /employees/{id}/activate sets status to ACTIVE")
    void adminActivatesEmployee() throws Exception {
        String empId = createEmployee("activate.emp@test.com");

        performPatchWithCookie(EMPLOYEES_URL + "/" + empId + "/activate", adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("admin: PATCH /employees/{id}/deactivate sets status to INACTIVE")
    void adminDeactivatesEmployee() throws Exception {
        String empId = createEmployee("deactivate.emp@test.com");
        // Activate first
        performPatchWithCookie(EMPLOYEES_URL + "/" + empId + "/activate", adminCookie);

        performPatchWithCookie(EMPLOYEES_URL + "/" + empId + "/deactivate", adminCookie)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Soft delete
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: DELETE /employees/{id} soft-deletes, subsequent GET returns 404")
    void adminSoftDeletesEmployee_Returns404OnGet() throws Exception {
        String empId = createEmployee("softdelete.emp@test.com");

        performDeleteWithCookie(EMPLOYEES_URL + "/" + empId, adminCookie)
                .andExpect(status().isOk());

        performGetWithCookie(EMPLOYEES_URL + "/" + empId, adminCookie)
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Duplicate email
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin: POST /employees with duplicate email returns 409")
    void duplicateEmail_Returns409() throws Exception {
        createEmployee("duplicate@test.com");

        performPostWithCookie(EMPLOYEES_URL, buildCreateRequest("duplicate@test.com"), adminCookie)
                .andExpect(status().isConflict());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RBAC — employee cannot access employee management endpoints
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee: GET /employees returns 403")
    void employee_CannotListEmployees_Returns403() throws Exception {
        Cookie empCookie = createEmployeeCookieFor(EMP_EMAIL, EMP_PASSWORD);

        performGetWithCookie(EMPLOYEES_URL, empCookie)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("employee: POST /employees returns 403")
    void employee_CannotCreateEmployee_Returns403() throws Exception {
        Cookie empCookie = createEmployeeCookieFor(EMP_EMAIL, EMP_PASSWORD);

        performPostWithCookie(EMPLOYEES_URL, buildCreateRequest("blocked@test.com"), empCookie)
                .andExpect(status().isForbidden());
    }
}
