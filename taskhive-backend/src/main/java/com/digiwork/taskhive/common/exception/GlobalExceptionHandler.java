package com.digiwork.taskhive.common.exception;

import com.digiwork.taskhive.common.constants.MessageConstants;
import com.digiwork.taskhive.common.dto.ErrorResponse;
import com.digiwork.taskhive.module.auth.exception.*;
import com.digiwork.taskhive.module.employee.exception.EmployeeAlreadyExistsException;
import com.digiwork.taskhive.module.employee.exception.EmployeeNotFoundException;
import com.digiwork.taskhive.module.task.exception.TaskAccessDeniedException;
import com.digiwork.taskhive.module.task.exception.TaskNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(FieldError::getDefaultMessage)
                                .toList();
                return ResponseEntity.badRequest()
                                .body(ErrorResponse.of(MessageConstants.VALIDATION_ERROR, errors,
                                                request.getRequestURI()));
        }

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(AccountLockedException.class)
        public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.LOCKED)
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(AccountNotActiveException.class)
        public ResponseEntity<ErrorResponse> handleAccountNotActive(AccountNotActiveException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler({ TokenExpiredException.class, TokenAlreadyUsedException.class,
                        com.digiwork.taskhive.module.auth.exception.InvalidTokenException.class })
        public ResponseEntity<ErrorResponse> handleTokenExceptions(RuntimeException ex, HttpServletRequest request) {
                return ResponseEntity.badRequest()
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<ErrorResponse> handleCommonInvalidToken(InvalidTokenException ex,
                        HttpServletRequest request) {
                return ResponseEntity.badRequest()
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
                return ResponseEntity.badRequest()
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(EmployeeNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleEmployeeNotFound(EmployeeNotFoundException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(EmployeeAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleEmployeeAlreadyExists(EmployeeAlreadyExistsException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ErrorResponse.of(MessageConstants.ACCESS_DENIED, request.getRequestURI()));
        }

        @ExceptionHandler(TaskNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleTaskNotFound(TaskNotFoundException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(TaskAccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleTaskAccessDenied(TaskAccessDeniedException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ErrorResponse.of(ex.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
                log.error("Unexpected error: ", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of(MessageConstants.INTERNAL_ERROR, request.getRequestURI()));
        }
}
