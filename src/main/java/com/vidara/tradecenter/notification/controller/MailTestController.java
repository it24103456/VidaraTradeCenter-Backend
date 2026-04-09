package com.vidara.tradecenter.notification.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.notification.config.EmailConfig;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dev-only SMTP check. Enable with {@code app.mail.test-endpoint-enabled=true} (see application-dev.properties).
 */
@RestController
@RequestMapping("/api/admin/mail")
@ConditionalOnProperty(name = "app.mail.test-endpoint-enabled", havingValue = "true")
@PreAuthorize("hasRole('ADMIN')")
public class MailTestController {

    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;

    public MailTestController(JavaMailSender mailSender, EmailConfig emailConfig) {
        this.mailSender = mailSender;
        this.emailConfig = emailConfig;
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<Void>> sendTest(@RequestParam(required = false) String to) {
        String recipient = (to != null && !to.isBlank()) ? to.trim() : emailConfig.getFromAddress();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(emailConfig.getFromAddress());
            helper.setTo(recipient);
            helper.setSubject("Vidara Trade Center — SMTP test");
            helper.setText("<p>If you received this, Gmail SMTP from the backend is working.</p>", true);
            mailSender.send(message);
            return ResponseEntity.ok(ApiResponse.success("Test email sent to " + recipient, null));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SMTP test failed: " + e.getMessage()));
        }
    }
}
