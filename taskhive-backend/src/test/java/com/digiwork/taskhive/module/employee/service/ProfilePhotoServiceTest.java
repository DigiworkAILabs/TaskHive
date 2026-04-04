package com.digiwork.taskhive.module.employee.service;

import com.digiwork.taskhive.common.exception.BusinessException;
import com.digiwork.taskhive.common.storage.StorageService;
import com.digiwork.taskhive.module.employee.exception.EmployeeNotFoundException;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfilePhotoServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private ProfilePhotoService profilePhotoService;

    private UUID employeeId;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        testEmployee = Employee.builder()
                .id(employeeId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .isDeleted(false)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // uploadPhoto Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("uploadPhoto")
    class UploadPhotoTests {

        @Test
        @DisplayName("should upload photo successfully")
        void shouldUploadPhotoSuccessfully() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getContentType()).thenReturn("image/jpeg");

            when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                    .thenReturn(Optional.of(testEmployee));
            when(storageService.store(file, "employees/photos", employeeId.toString()))
                    .thenReturn("employees/photos/" + employeeId);
            when(storageService.getUrl("employees/photos/" + employeeId))
                    .thenReturn("http://localhost/photos/" + employeeId);

            String url = profilePhotoService.uploadPhoto(employeeId, file);

            assertThat(url).isEqualTo("http://localhost/photos/" + employeeId);
            verify(employeeRepository).save(testEmployee);
        }

        @Test
        @DisplayName("should delete old photo before uploading new one")
        void shouldDeleteOldPhotoBeforeUpload() throws IOException {
            testEmployee.setPhotoUrl("employees/photos/oldphoto");
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getContentType()).thenReturn("image/png");

            when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                    .thenReturn(Optional.of(testEmployee));
            when(storageService.store(file, "employees/photos", employeeId.toString()))
                    .thenReturn("employees/photos/" + employeeId);
            when(storageService.getUrl(any())).thenReturn("http://localhost/photo");

            profilePhotoService.uploadPhoto(employeeId, file);

            verify(storageService).delete("employees/photos/oldphoto");
        }

        @Test
        @DisplayName("should throw EmployeeNotFoundException when employee not found")
        void shouldThrowException_whenEmployeeNotFound() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getContentType()).thenReturn("image/jpeg");

            when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> profilePhotoService.uploadPhoto(employeeId, file))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // validateFile Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validateFile")
    class ValidateFileTests {

        @Test
        @DisplayName("should throw BusinessException when file is empty")
        void shouldThrowException_whenFileIsEmpty() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            assertThatThrownBy(() -> profilePhotoService.uploadPhoto(employeeId, file))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("File is required");
        }

        @Test
        @DisplayName("should throw BusinessException when file is null")
        void shouldThrowException_whenFileIsNull() {
            assertThatThrownBy(() -> profilePhotoService.uploadPhoto(employeeId, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("File is required");
        }

        @Test
        @DisplayName("should throw BusinessException when file exceeds 5MB")
        void shouldThrowException_whenFileTooLarge() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(6 * 1024 * 1024L); // 6MB

            assertThatThrownBy(() -> profilePhotoService.uploadPhoto(employeeId, file))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("5MB");
        }

        @Test
        @DisplayName("should throw BusinessException when file type is not allowed")
        void shouldThrowException_whenFileTypeNotAllowed() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getContentType()).thenReturn("application/pdf");

            assertThatThrownBy(() -> profilePhotoService.uploadPhoto(employeeId, file))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("JPG, PNG, and WebP");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getPhotoUrl Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPhotoUrl")
    class GetPhotoUrlTests {

        @Test
        @DisplayName("should return photo URL when photo exists")
        void shouldGetPhotoUrl() {
            testEmployee.setPhotoUrl("employees/photos/test");
            when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                    .thenReturn(Optional.of(testEmployee));
            when(storageService.getUrl("employees/photos/test"))
                    .thenReturn("http://localhost/photos/test");

            String url = profilePhotoService.getPhotoUrl(employeeId);

            assertThat(url).isEqualTo("http://localhost/photos/test");
        }

        @Test
        @DisplayName("should return null when no photo exists")
        void shouldReturnNull_whenNoPhoto() {
            testEmployee.setPhotoUrl(null);
            when(employeeRepository.findByIdAndIsDeletedFalse(employeeId))
                    .thenReturn(Optional.of(testEmployee));

            String url = profilePhotoService.getPhotoUrl(employeeId);

            assertThat(url).isNull();
        }
    }
}
