package com.digiwork.taskhive.common.util;

import com.digiwork.taskhive.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileValidationUtilTest {

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "application/pdf");

    // A tiny valid JPEG byte array (start of file marker)
    private static final byte[] VALID_JPEG_BYTES = new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 'J', 'F', 'I', 'F', 0x00
    };

    @Test
    @DisplayName("should detect real MIME type")
    void shouldDetectMimeType() {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", VALID_JPEG_BYTES);
        
        String detected = FileValidationUtil.detectMimeType(file);
        assertThat(detected).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("should pass validation for valid content type")
    void shouldPassValidation() {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", VALID_JPEG_BYTES);
        
        FileValidationUtil.validateContentType(file, ALLOWED_TYPES);
        // implicit assertion: no exception thrown
    }

    @Test
    @DisplayName("should reject spoofed file types")
    void shouldRejectSpoofedTypes() {
        // An HTML file falsely claiming to be an image
        byte[] htmlBytes = "<html><script>alert(1)</script></html>".getBytes();
        MultipartFile file = new MockMultipartFile("file", "malicious.jpg", "image/jpeg", htmlBytes);

        assertThatThrownBy(() -> FileValidationUtil.validateContentType(file, ALLOWED_TYPES))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("File type not allowed");
    }

    @Test
    @DisplayName("should get safe extension matching original if valid")
    void shouldGetSafeExtension() {
        MultipartFile file = new MockMultipartFile("file", "test.jpeg", "image/jpeg", VALID_JPEG_BYTES);
        
        String ext = FileValidationUtil.getSafeExtension(file, ALLOWED_TYPES);
        assertThat(ext).isEqualTo("jpeg");
    }
    
    @Test
    @DisplayName("should fallback to default extension if original is invalid")
    void shouldFallbackExtension() {
        MultipartFile file = new MockMultipartFile("file", "test.jsp", "image/jpeg", VALID_JPEG_BYTES);
        
        String ext = FileValidationUtil.getSafeExtension(file, ALLOWED_TYPES);
        assertThat(ext).isIn("jpg", "jpeg");
    }
}
