package com.digiwork.taskhive.common.exception;

import com.digiwork.taskhive.common.constants.MessageConstants;
import com.digiwork.taskhive.common.dto.ErrorResponse;
import com.digiwork.taskhive.module.audit.service.AuditService;
import com.digiwork.taskhive.module.auth.exception.*;
import com.digiwork.taskhive.module.employee.exception.EmployeeAlreadyExistsException;
import com.digiwork.taskhive.module.employee.exception.EmployeeNotFoundException;
import com.digiwork.taskhive.module.task.exception.TaskAccessDeniedException;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler — validates every exception → HTTP status mapping.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private AuditService auditService;

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(auditService);
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/test");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(request.getMethod()).thenReturn("GET");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Authentication / Authorization Exceptions
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Authentication & Authorization")
    class AuthExceptions {

        @Test
        @DisplayName("InvalidCredentialsException → 401 UNAUTHORIZED")
        void invalidCredentials() {
            ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(
                    new InvalidCredentialsException("Invalid email or password"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("Invalid email or password");
            assertThat(response.getBody().isSuccess()).isFalse();
        }

        @Test
        @DisplayName("AccountLockedException → 423 LOCKED")
        void accountLocked() {
            ResponseEntity<ErrorResponse> response = handler.handleAccountLocked(
                    new AccountLockedException("Account locked"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
            assertThat(response.getBody().getMessage()).contains("Account locked");
        }

        @Test
        @DisplayName("AccountNotActiveException → 403 FORBIDDEN")
        void accountNotActive() {
            ResponseEntity<ErrorResponse> response = handler.handleAccountNotActive(
                    new AccountNotActiveException("Account not active"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("UnauthorizedException → 401 UNAUTHORIZED")
        void unauthorized() {
            ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(
                    new UnauthorizedException("Unauthorized"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("AccessDeniedException → 403 FORBIDDEN + audit log")
        void accessDenied() {
            ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(
                    new AccessDeniedException("Access denied"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().getMessage()).isEqualTo(MessageConstants.ACCESS_DENIED);
            verify(auditService).logSecurityEvent(eq("UNAUTHORIZED_ACCESS"), isNull(), eq("127.0.0.1"),
                    eq(false), anyString());
        }

        @Test
        @DisplayName("AccessDeniedException audit failure does not bubble up")
        void accessDenied_auditFailureSilenced() {
            doThrow(new RuntimeException("DB down")).when(auditService)
                    .logSecurityEvent(anyString(), any(), anyString(), anyBoolean(), anyString());

            ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(
                    new AccessDeniedException("Access denied"), request);

            // Should still return 403 even if audit logging fails
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Token Exceptions
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Token Exceptions")
    class TokenExceptions {

        @Test
        @DisplayName("TokenExpiredException → 400 BAD_REQUEST")
        void tokenExpired() {
            ResponseEntity<ErrorResponse> response = handler.handleTokenExceptions(
                    new TokenExpiredException("Token expired"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("TokenAlreadyUsedException → 400 BAD_REQUEST")
        void tokenAlreadyUsed() {
            ResponseEntity<ErrorResponse> response = handler.handleTokenExceptions(
                    new TokenAlreadyUsedException("Token used"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Auth InvalidTokenException → 400 BAD_REQUEST")
        void authInvalidToken() {
            ResponseEntity<ErrorResponse> response = handler.handleTokenExceptions(
                    new com.digiwork.taskhive.module.auth.exception.InvalidTokenException("Invalid token"),
                    request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Common InvalidTokenException → 400 BAD_REQUEST")
        void commonInvalidToken() {
            ResponseEntity<ErrorResponse> response = handler.handleCommonInvalidToken(
                    new InvalidTokenException("Invalid token"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Resource Exceptions
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Resource Exceptions")
    class ResourceExceptions {

        @Test
        @DisplayName("ResourceNotFoundException → 404 NOT_FOUND")
        void resourceNotFound() {
            ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                    new ResourceNotFoundException("Resource not found"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("DuplicateResourceException → 409 CONFLICT")
        void duplicateResource() {
            ResponseEntity<ErrorResponse> response = handler.handleDuplicate(
                    new DuplicateResourceException("Already exists"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("EmployeeNotFoundException → 404 NOT_FOUND")
        void employeeNotFound() {
            ResponseEntity<ErrorResponse> response = handler.handleEmployeeNotFound(
                    new EmployeeNotFoundException("Employee not found"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("EmployeeAlreadyExistsException → 409 CONFLICT")
        void employeeAlreadyExists() {
            ResponseEntity<ErrorResponse> response = handler.handleEmployeeAlreadyExists(
                    new EmployeeAlreadyExistsException("Employee exists"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("TaskNotFoundException → 404 NOT_FOUND")
        void taskNotFound() {
            ResponseEntity<ErrorResponse> response = handler.handleTaskNotFound(
                    new TaskNotFoundException("Task not found"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("TaskAccessDeniedException → 403 FORBIDDEN")
        void taskAccessDenied() {
            ResponseEntity<ErrorResponse> response = handler.handleTaskAccessDenied(
                    new TaskAccessDeniedException("Not your task"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Business & Validation Exceptions
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Business & Validation")
    class BusinessExceptions {

        @Test
        @DisplayName("BusinessException without error code → 400 BAD_REQUEST")
        void businessExceptionWithoutCode() {
            ResponseEntity<ErrorResponse> response = handler.handleBusiness(
                    new BusinessException("Something went wrong"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).isEqualTo("Something went wrong");
            assertThat(response.getBody().getErrors()).isNull();
        }

        @Test
        @DisplayName("BusinessException with error code → 400 BAD_REQUEST + code in errors list")
        void businessExceptionWithCode() {
            ResponseEntity<ErrorResponse> response = handler.handleBusiness(
                    new BusinessException("TASK_3004", "Cancellation reason is required"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).isEqualTo("Cancellation reason is required");
            assertThat(response.getBody().getErrors()).containsExactly("TASK_3004");
        }

        @Test
        @DisplayName("MethodArgumentNotValidException → 400 BAD_REQUEST with field errors")
        void validationException() {
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError1 = new FieldError("request", "title", "Title is required");
            FieldError fieldError2 = new FieldError("request", "priority", "Priority is required");
            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

            ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).isEqualTo(MessageConstants.VALIDATION_ERROR);
            assertThat(response.getBody().getErrors()).containsExactlyInAnyOrder(
                    "Title is required", "Priority is required");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Generic Exception (Catch-All)
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Generic Exception")
    class GenericException {

        @Test
        @DisplayName("Unhandled Exception → 500 INTERNAL_SERVER_ERROR")
        void genericException() {
            ResponseEntity<ErrorResponse> response = handler.handleGeneral(
                    new RuntimeException("Unexpected NPE"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getMessage()).isEqualTo(MessageConstants.INTERNAL_ERROR);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Response Structure Checks
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Response Structure")
    class ResponseStructure {

        @Test
        @DisplayName("All error responses include path from request URI")
        void errorResponseIncludesPath() {
            ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                    new ResourceNotFoundException("Test"), request);

            assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
        }

        @Test
        @DisplayName("All error responses have success=false")
        void errorResponseHasSuccessFalse() {
            ResponseEntity<ErrorResponse> response = handler.handleBusiness(
                    new BusinessException("Test"), request);

            assertThat(response.getBody().isSuccess()).isFalse();
        }
    }
}
