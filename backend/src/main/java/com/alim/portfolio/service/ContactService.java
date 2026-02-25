package com.alim.portfolio.service;

import com.alim.portfolio.dto.ContactRequest;
import com.alim.portfolio.model.ContactMessage;
import com.alim.portfolio.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final JavaMailSender mailSender;

    @Value("${portfolio.owner.email}")
    private String ownerEmail;

    public void processContactMessage(ContactRequest request) {
        // Save to database
        ContactMessage message = ContactMessage.builder()
                .name(request.getName())
                .email(request.getEmail())
                .subject(request.getSubject() != null ? request.getSubject() : "Portfolio Contact")
                .message(request.getMessage())
                .build();

        contactMessageRepository.save(message);
        log.info("Contact message saved from: {}", request.getEmail());

        // Send email notification
        try {
            sendNotificationEmail(request);
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage());
            // Don't fail the request if email fails
        }
    }

    private void sendNotificationEmail(ContactRequest request) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(ownerEmail);
        mail.setSubject("[Portfolio] New message from " + request.getName());
        mail.setText(String.format("""
                You have a new message from your portfolio!
                
                Name: %s
                Email: %s
                Subject: %s
                
                Message:
                %s
                
                ---
                Sent from your portfolio contact form.
                """,
                request.getName(),
                request.getEmail(),
                request.getSubject(),
                request.getMessage()
        ));
        mailSender.send(mail);
        log.info("Email notification sent for contact from: {}", request.getEmail());
    }
}
