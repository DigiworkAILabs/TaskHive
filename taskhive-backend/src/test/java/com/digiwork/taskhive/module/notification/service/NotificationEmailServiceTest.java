package com.digiwork.taskhive.module.notification.service;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private NotificationEmailService notificationEmailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        // Need a real-ish MimeMessage for the captor to work correctly with getRecipients/getSubject
        mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
    }

    @Test
    @DisplayName("sendEmailFromTemplate: should verify recipient and subject in constructed MimeMessage")
    void sendEmailFromTemplate_Success() throws Exception {
        String recipient = "to@example.com";
        String subject = "Test Subject";
        String template = "test-template";
        Map<String, Object> data = Map.of("name", "User");

        when(templateEngine.process(eq("email/" + template), any(Context.class))).thenReturn("<html>Body</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        notificationEmailService.sendEmailFromTemplate(recipient, subject, template, data);

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        MimeMessage captured = messageCaptor.getValue();
        assertThat(captured.getRecipients(Message.RecipientType.TO)[0]).hasToString(recipient);
        assertThat(captured.getSubject()).isEqualTo(subject);
    }

    @Test
    @DisplayName("sendEmailFromTemplate: should propagate MessagingException if mail server setup fails")
    void sendEmailFromTemplate_MailSenderFailure() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");
        
        // Mock send to throw
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        Map<String, Object> data = Map.of();
        assertThatThrownBy(() -> 
            notificationEmailService.sendEmailFromTemplate("to@ex.com", "Sub", "tmp", data)
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("sendEmailFromTemplate: should throw if template processing fails")
    void sendEmailFromTemplate_TemplateFailure() {
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("Template not found"));

        Map<String, Object> data = Map.of();
        assertThatThrownBy(() -> 
            notificationEmailService.sendEmailFromTemplate("to@ex.com", "Sub", "tmp", data)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("Template not found");

        verifyNoInteractions(mailSender);
    }
}
