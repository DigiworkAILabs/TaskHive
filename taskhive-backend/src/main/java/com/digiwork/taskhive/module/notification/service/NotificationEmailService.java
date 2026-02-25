package com.digiwork.taskhive.module.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Sends an HTML email using a Thymeleaf template.
     * This method is called by the EmailQueueScheduler, NOT directly by listeners.
     */
    public void sendEmailFromTemplate(String to, String subject, String templateName,
            Map<String, Object> variables) throws MessagingException {
        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process("email/" + templateName, context);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
        log.info("Email sent successfully to [{}] with template [{}]", to, templateName);
    }
}
