package com.digiwork.taskhive.module.auth.controller;

import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.module.auth.dto.GdprExportResponse;
import com.digiwork.taskhive.module.auth.service.GdprService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * GDPR endpoints — data export and right-to-be-forgotten (NFR-SEC-13).
 *
 * Security: users can only act on their own data; ADMIN can act on any user.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "GDPR", description = "GDPR data export and deletion (NFR-SEC-13)")
public class GdprController {

    private final GdprService gdprService;

    /**
     * Export all personal data for a user as a JSON response.
     * Accessible by the user themselves or by an ADMIN.
     */
    @GetMapping("/{userId}/data-export")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Operation(summary = "Export personal data", description = "Returns all personal data stored for the given user (GDPR Art. 20)")
    public ResponseEntity<GdprExportResponse> exportUserData(@PathVariable UUID userId) {
        return ResponseEntity.ok(gdprService.exportUserData(userId));
    }

    /**
     * Anonymize (right to be forgotten) a user's personal data.
     * Accessible by the user themselves or by an ADMIN.
     */
    @PostMapping("/{userId}/gdpr-delete")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @Operation(summary = "Anonymize personal data", description = "Replaces all PII with anonymized values (GDPR Art. 17)")
    public ResponseEntity<ApiResponse<Void>> anonymizeUserData(@PathVariable UUID userId) {
        gdprService.anonymizeUserData(userId);
        return ResponseEntity.ok(ApiResponse.success("User data anonymized successfully", null));
    }
}
