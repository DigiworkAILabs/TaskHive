package com.digiwork.taskhive.module.auth.controller;

import com.digiwork.taskhive.common.constants.CookieConstants;
import com.digiwork.taskhive.common.constants.MessageConstants;
import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.module.auth.dto.*;
import com.digiwork.taskhive.module.auth.service.AccountActivationService;
import com.digiwork.taskhive.module.auth.service.AuthService;
import com.digiwork.taskhive.module.auth.service.PasswordResetService;
import com.digiwork.taskhive.common.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final AccountActivationService accountActivationService;
    private final PasswordResetService passwordResetService;
    private final CookieUtil cookieUtil;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user and sets JWT cookies")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.LOGIN_SUCCESS, loginResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revokes refresh token and clears cookies")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = cookieUtil.extractCookieValue(request, CookieConstants.REFRESH_TOKEN_COOKIE);
        authService.logout(refreshToken, response);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.LOGOUT_SUCCESS));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Issues a new access token using refresh token cookie")
    public ResponseEntity<ApiResponse<Void>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = cookieUtil.extractCookieValue(request, CookieConstants.REFRESH_TOKEN_COOKIE);
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(MessageConstants.REFRESH_TOKEN_MISSING));
        }
        authService.refresh(refreshToken, response);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.TOKEN_REFRESHED));
    }

    @PostMapping("/activate-account")
    @Operation(summary = "Activate Account", description = "Employee sets password via activation token from email")
    public ResponseEntity<ApiResponse<Void>> activateAccount(
            @Valid @RequestBody ActivateAccountRequest request) {
        accountActivationService.activateAccount(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.ACCOUNT_ACTIVATED));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot Password", description = "Sends password reset email (no email enumeration)")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        // Always return same message to prevent email enumeration
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PASSWORD_RESET_EMAIL_SENT));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password", description = "Resets password using token from email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PASSWORD_RESET_SUCCESS));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change Password", description = "Change own password (requires JWT)")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletResponse response) {
        authService.changePassword(request, response);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.PASSWORD_CHANGED));
    }

    @GetMapping("/me")
    @Operation(summary = "Get Current User", description = "Returns info about the currently authenticated user")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        UserInfoResponse userInfo = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("User info retrieved", userInfo));
    }
}
