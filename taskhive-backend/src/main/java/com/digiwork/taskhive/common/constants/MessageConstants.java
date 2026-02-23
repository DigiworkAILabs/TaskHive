package com.digiwork.taskhive.common.constants;

public final class MessageConstants {

    private MessageConstants() {
    }

    // Auth
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGOUT_SUCCESS = "Logout successful";
    public static final String TOKEN_REFRESHED = "Token refreshed successfully";
    public static final String ACCOUNT_ACTIVATED = "Account activated successfully";
    public static final String PASSWORD_RESET_EMAIL_SENT = "If an account with that email exists, a password reset link has been sent";
    public static final String PASSWORD_RESET_SUCCESS = "Password reset successfully";
    public static final String PASSWORD_CHANGED = "Password changed successfully";

    // Errors
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String ACCOUNT_LOCKED = "Account is locked. Please try again later";
    public static final String ACCOUNT_NOT_ACTIVE = "Account is not active";
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String TOKEN_ALREADY_USED = "Token has already been used";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String UNAUTHORIZED = "Unauthorized access";
    public static final String PASSWORD_SAME_AS_OLD = "New password cannot be the same as any of your last 3 passwords";
    public static final String INVALID_OLD_PASSWORD = "Current password is incorrect";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String DUPLICATE_RESOURCE = "Resource already exists";
    public static final String INTERNAL_ERROR = "An unexpected error occurred";
    public static final String VALIDATION_ERROR = "Validation failed";
    public static final String ACCESS_DENIED = "Access denied";
    public static final String REFRESH_TOKEN_MISSING = "Refresh token is missing";

    // Employee
    public static final String EMPLOYEE_CREATED = "Employee created successfully";
    public static final String EMPLOYEE_UPDATED = "Employee updated successfully";
    public static final String EMPLOYEE_DELETED = "Employee deleted successfully";
    public static final String EMPLOYEE_ACTIVATED = "Employee activated successfully";
    public static final String EMPLOYEE_DEACTIVATED = "Employee deactivated successfully";
    public static final String EMPLOYEE_NOT_FOUND = "Employee not found";
    public static final String EMPLOYEE_ALREADY_EXISTS = "Employee with this email already exists";

    // Task
    public static final String TASK_CREATED = "Task created successfully";
    public static final String TASK_UPDATED = "Task updated successfully";
    public static final String TASK_DELETED = "Task deleted successfully";
    public static final String TASK_NOT_FOUND = "Task not found";
    public static final String TASK_STATUS_UPDATED = "Task status updated successfully";
    public static final String TASK_ACCESS_DENIED = "You do not have access to this task";
    public static final String TASK_COMMENT_ADDED = "Comment added successfully";
    public static final String TASK_ATTACHMENT_UPLOADED = "Attachment uploaded successfully";
}
