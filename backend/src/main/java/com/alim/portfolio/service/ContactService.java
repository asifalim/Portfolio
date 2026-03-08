package com.alim.portfolio.service;

import com.alim.portfolio.dto.ContactRequest;
import com.alim.portfolio.model.ContactMessage;
import com.alim.portfolio.repository.ContactMessageRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final SendGrid sendGrid;

    @Value("${sendgrid.verified.email}")
    private String verifiedEmail;

    @Value("${sendgrid.recipient.email}")
    private String recipientEmail;

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
        Email from = new Email(verifiedEmail);
        Email recipient = new Email(recipientEmail);

        Content content = new Content("text/plain", request.getMessage());
        Mail mail = new Mail(from, request.getSubject(), recipient, content);
        mail.setReplyTo(new Email(request.getEmail()));
        Request request1 = new Request();
        try {
            request1.setMethod(Method.POST);
            request1.setEndpoint("mail/send");
            request1.setBody(mail.build());

            Response response = sendGrid.api(request1);

            System.out.println("Status Code: " + response.getStatusCode());

        } catch (IOException ex) {
            throw new RuntimeException("Failed to send email", ex);
        }
    }
}
