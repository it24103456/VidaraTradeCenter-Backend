package com.vidara.tradecenter.contact.service;

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.contact.dto.ContactInquiryRequest;
import com.vidara.tradecenter.notification.config.EmailConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ContactInquiryService {

    private static final Logger log = LoggerFactory.getLogger(ContactInquiryService.class);

    private static final Map<String, String> SUBJECT_LABELS = Map.of(
            "general", "General inquiry",
            "order", "Order & delivery",
            "returns", "Returns & refunds",
            "product", "Product question",
            "partnership", "Partnership / wholesale",
            "other", "Other"
    );

    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;

    @Value("${app.mail.contact-inbox:}")
    private String contactInbox;

    public ContactInquiryService(JavaMailSender mailSender, EmailConfig emailConfig) {
        this.mailSender = mailSender;
        this.emailConfig = emailConfig;
    }

    public void sendInquiry(ContactInquiryRequest req) {
        String to = (contactInbox != null && !contactInbox.isBlank())
                ? contactInbox.trim()
                : emailConfig.getFromAddress();

        String topicKey = req.getSubject() != null ? req.getSubject().trim().toLowerCase() : "general";
        String topicLabel = SUBJECT_LABELS.getOrDefault(topicKey, req.getSubject());

        String safeName = req.getName() != null ? req.getName().trim() : "";
        String safeEmail = req.getEmail() != null ? req.getEmail().trim() : "";
        String safePhone = req.getPhone() != null ? req.getPhone().trim() : "";
        String safeMessage = req.getMessage() != null ? req.getMessage().trim() : "";

        String body = """
                New message from the Contact Us form on Vidara Trade Center.

                Topic: %s
                Name: %s
                Email: %s
                Phone: %s

                Message:
                %s
                """.formatted(topicLabel, safeName, safeEmail,
                safePhone.isEmpty() ? "(not provided)" : safePhone,
                safeMessage);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            try {
                InternetAddress from = new InternetAddress(
                        emailConfig.getFromAddress(),
                        emailConfig.getFromName());
                helper.setFrom(from);
            } catch (java.io.UnsupportedEncodingException e) {
                helper.setFrom(emailConfig.getFromAddress());
            }
            helper.setTo(to);
            helper.setReplyTo(safeEmail);
            helper.setSubject("[Contact] " + topicLabel + " — " + safeName);
            helper.setText(body, false);
            mailSender.send(message);
            log.info("Contact inquiry email sent to inbox for topic {} from {}", topicKey, safeEmail);
        } catch (MessagingException e) {
            log.error("Failed to send contact inquiry email: {}", e.getMessage(), e);
            throw new BadRequestException(
                    "We could not send your message right now. Please try again later or email us directly.");
        }
    }
}
