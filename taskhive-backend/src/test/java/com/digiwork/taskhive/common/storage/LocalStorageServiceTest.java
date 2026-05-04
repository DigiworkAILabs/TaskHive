package com.digiwork.taskhive.common.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class LocalStorageServiceTest {

    @InjectMocks
    private LocalStorageService storageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(storageService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(storageService, "baseUrl", "http://localhost:8080/uploads");
    }

    @Test
    @DisplayName("should store file and return relative path")
    void shouldStoreFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "test content".getBytes());
        
        String relativePath = storageService.store(file, "avatars", "user-1", "png");

        assertThat(relativePath).isEqualTo("avatars/user-1.png");
        assertThat(Files.exists(tempDir.resolve("avatars/user-1.png"))).isTrue();
    }

    @Test
    @DisplayName("should delete existing file")
    void shouldDeleteFile() throws IOException {
        Path filePath = tempDir.resolve("test.txt");
        Files.writeString(filePath, "content");

        storageService.delete("test.txt");

        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    @DisplayName("should not throw exception when deleting non-existent file")
    void shouldHandleDeleteNonExistent() {
        assertThatCode(() -> storageService.delete("non-existent.txt"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should handle null path in delete")
    void shouldHandleDeleteNull() {
        assertThatCode(() -> storageService.delete(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should return correct URL")
    void shouldReturnUrl() {
        String url = storageService.getUrl("photos/my-pic.jpg");
        assertThat(url).isEqualTo("http://localhost:8080/uploads/photos/my-pic.jpg");
        
        assertThat(storageService.getUrl(null)).isNull();
    }

    @Test
    @DisplayName("should handle extension appropriately")
    void shouldHandleExtension() throws IOException {
        MockMultipartFile f1 = new MockMultipartFile("f", "image.PNG", "image/png", "c".getBytes());
        String p1 = storageService.store(f1, "t", "n1", "png");
        assertThat(p1).endsWith(".png");

        MockMultipartFile f2 = new MockMultipartFile("f", "noextension", "image/jpeg", "c".getBytes());
        String p2 = storageService.store(f2, "t", "n2", null);
        assertThat(p2).endsWith(".bin"); // Default extension

        MockMultipartFile f3 = new MockMultipartFile("f", null, "image/jpeg", "c".getBytes());
        String p3 = storageService.store(f3, "t", "n3", "   ");
        assertThat(p3).endsWith(".bin"); // Handle blank extension fallback
    }
}
