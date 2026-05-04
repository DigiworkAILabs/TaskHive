package com.digiwork.taskhive.common.util;

import com.digiwork.taskhive.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public final class FileValidationUtil {

    private static final Tika TIKA = new Tika();

    // Map of allowed MIME type -> permitted extensions
    private static final Map<String, Set<String>> ALLOWED_EXTENSIONS = Map.ofEntries(
            Map.entry("image/jpeg", Set.of("jpg", "jpeg")),
            Map.entry("image/png", Set.of("png")),
            Map.entry("image/webp", Set.of("webp")),
            Map.entry("image/gif", Set.of("gif")),
            Map.entry("application/pdf", Set.of("pdf")),
            Map.entry("application/msword", Set.of("doc")),
            Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document", Set.of("docx")),
            Map.entry("application/vnd.ms-excel", Set.of("xls")),
            Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Set.of("xlsx")),
            Map.entry("application/vnd.ms-powerpoint", Set.of("ppt")),
            Map.entry("application/vnd.openxmlformats-officedocument.presentationml.presentation", Set.of("pptx")),
            Map.entry("text/plain", Set.of("txt")),
            Map.entry("text/csv", Set.of("csv")),
            Map.entry("application/zip", Set.of("zip")),
            Map.entry("application/x-rar-compressed", Set.of("rar"))
    );

    private FileValidationUtil() {
    }

    /**
     * Detects real MIME type using Tika magic bytes
     */
    public static String detectMimeType(MultipartFile file) {
        try {
            return TIKA.detect(file.getInputStream());
        } catch (IOException e) {
            log.error("Failed to detect file MIME type", e);
            throw new BusinessException("Failed to validate file content");
        }
    }

    /**
     * Validates that the detected MIME type is in the allowed set.
     */
    public static void validateContentType(MultipartFile file, List<String> allowedTypes) {
        String detectedMimeType = detectMimeType(file);
        
        if (!allowedTypes.contains(detectedMimeType)) {
            log.warn("Blocked file upload: Detected MIME {} is not in allowed list", detectedMimeType);
            throw new BusinessException("File type not allowed. Supported types are strictly enforced.");
        }
        
        // Log mismatch between claimed header and actual magic bytes
        String claimedMimeType = file.getContentType();
        if (claimedMimeType != null && !claimedMimeType.equalsIgnoreCase(detectedMimeType)) {
            log.warn("MIME type spoofing attempt detected. Claimed: {}, Actual (magic bytes): {}", 
                     claimedMimeType, detectedMimeType);
        }
    }

    /**
     * Extracts a safe, whitelisted extension based on the actual detected MIME type.
     */
    public static String getSafeExtension(MultipartFile file, List<String> allowedTypes) {
        String detectedMimeType = detectMimeType(file);
        
        if (!allowedTypes.contains(detectedMimeType)) {
            throw new BusinessException("Invalid file content");
        }
        
        Set<String> validExtensions = ALLOWED_EXTENSIONS.get(detectedMimeType);
        if (validExtensions == null || validExtensions.isEmpty()) {
            return "bin"; // safe fallback
        }
        
        // Check original extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot > 0 && lastDot < originalFilename.length() - 1) {
                String originalExt = originalFilename.substring(lastDot + 1).toLowerCase();
                if (validExtensions.contains(originalExt)) {
                    return originalExt;
                }
            }
        }
        
        // Return first default from set if original extension doesn't match MIME or wasn't provided
        return validExtensions.iterator().next();
    }
}
