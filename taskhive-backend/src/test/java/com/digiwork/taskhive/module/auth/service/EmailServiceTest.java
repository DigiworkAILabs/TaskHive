package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.module.auth.event.PasswordChangedEvent;
import com.digiwork.taskhive.module.auth.event.PasswordResetRequestedEvent;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.employee.event.EmployeeCreatedEvent;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailService emailService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .firstName("John")
                .email("john@example.com")
                .build();

        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@taskhive.com");
    }

    @Test
    @DisplayName("handlePasswordResetRequested: should send email")
    void handlePasswordResetRequested() {
        PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(this, userId, "john@example.com", "token");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(templateEngine.process(eq("email/password-reset"), any(Context.class))).thenReturn("<html></html>");
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        emailService.handlePasswordResetRequested(event);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("handlePasswordChanged: should send email")
    void handlePasswordChanged() {
        PasswordChangedEvent event = new PasswordChangedEvent(this, userId, "john@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(templateEngine.process(eq("email/password-changed"), any(Context.class))).thenReturn("<html></html>");
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        emailService.handlePasswordChanged(event);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("handleEmployeeCreated: should send email")
    void handleEmployeeCreated() {
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(this, UUID.randomUUID(), userId, "John", "john@example.com", "token", UUID.randomUUID());
        when(templateEngine.process(eq("email/account-activation"), any(Context.class))).thenReturn("<html></html>");
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        emailService.handleEmployeeCreated(event);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("handleEmployeeCreated: should handle failure gracefully")
    void handleEmployeeCreated_Failure() {
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(this, UUID.randomUUID(), userId, "John", "john@example.com", "token", UUID.randomUUID());
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        
        doThrow(new org.springframework.mail.MailSendException("error")).when(mailSender).send(any(MimeMessage.class));

        // Should not throw exception
        emailService.handleEmployeeCreated(event);

        verify(mailSender).send(any(MimeMessage.class));
    }
}
