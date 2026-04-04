package com.digiwork.taskhive.module.auth.service;

import com.digiwork.taskhive.module.auth.event.PasswordChangedEvent;
import com.digiwork.taskhive.module.auth.event.PasswordResetRequestedEvent;
import com.digiwork.taskhive.module.employee.event.EmployeeCreatedEvent;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;
    private static final String VAR_FIRST_NAME = "firstName";

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @EventListener
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
        User user = userRepository.findById(event.getUserId()).orElse(null);
        if (user == null)
            return;

        String resetLink = frontendUrl + "/reset-password?token=" + event.getResetToken();

        Context context = new Context();
        context.setVariable(VAR_FIRST_NAME, user.getFirstName());
        context.setVariable("resetLink", resetLink);

        String htmlContent = templateEngine.process("email/password-reset", context);
        sendEmail(event.getEmail(), "Reset Your Password - TaskHive", htmlContent);
    }

    @Async
    @EventListener
    public void handlePasswordChanged(PasswordChangedEvent event) {
        User user = userRepository.findById(event.getUserId()).orElse(null);
        if (user == null)
            return;

        Context context = new Context();
        context.setVariable(VAR_FIRST_NAME, user.getFirstName());

        String htmlContent = templateEngine.process("email/password-changed", context);
        sendEmail(event.getEmail(), "Password Changed - TaskHive", htmlContent);
    }

    @Async
    @EventListener
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        String activationLink = frontendUrl + "/activate-account?token=" + event.getActivationToken();

        Context context = new Context();
        context.setVariable(VAR_FIRST_NAME, event.getFirstName());
        context.setVariable("activationLink", activationLink);

        String htmlContent = templateEngine.process("email/account-activation", context);
        sendEmail(event.getEmail(), "Activate Your Account - TaskHive", htmlContent);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email sent to: {} with subject: {}", to, subject);
        } catch (MessagingException | org.springframework.mail.MailException e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
}
