package com.uca.juangarcia.ifit.modules.notification.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.uca.juangarcia.ifit.exception.EmailNotFoundException;
import com.uca.juangarcia.ifit.modules.notification.dto.EmailResponseDto;
import com.uca.juangarcia.ifit.modules.notification.dto.SupportTicketRequestDto;
import com.uca.juangarcia.ifit.modules.notification.model.AppEmailDetails;
import com.uca.juangarcia.ifit.modules.user.dto.AppUserResponseDto;

import jakarta.mail.internet.MimeMessage;

@Service
public class AppEmailService {

    private static final Logger logger = LoggerFactory.getLogger(AppEmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${ifit.support.email}")
    private String supportEmail;

    /**
     * This method is used to send an email with the provided details.
     * 
     * @param details The details of the email to be sent.
     */
    public void sendEmail(AppEmailDetails details) {
        try {
            MimeMessage mailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(details.getRecipient());
            helper.setSubject(details.getSubject());
            helper.setText(details.getMsgBody(), true); // ← HTML activado

            javaMailSender.send(mailMessage);
        } catch (MailException | jakarta.mail.MessagingException e) {
            logger.error("Error sending email to {}: {}", details.getRecipient(), e.getMessage(), e);
        }
    }

    /**
     * Sends a verification email to the specified email address.
     * 
     * @param email The recipient's email address.
     * @return A confirmation message indicating that the verification email has been sent.
     * @throws EmailNotFoundException 
     */
    public EmailResponseDto sendVerificationEmail(AppUserResponseDto user) throws EmailNotFoundException {
        try {
            if (user == null || user.getEmail() == null) {
                throw new EmailNotFoundException(user != null ? user.getEmail() : "unknown");
            }
            
            AppEmailDetails details = new AppEmailDetails();
            details.setRecipient(user.getEmail());
            details.setSubject("Email Verification");

            Resource resource = new ClassPathResource("templates/email/verificationmail.html");
            String htmlTemplate = new String(
                resource.getInputStream().readAllBytes(), 
                StandardCharsets.UTF_8
            );
            
            String htmlContent = htmlTemplate.replace("{{verificationCode}}", user.getVerificationCode());
            details.setMsgBody(htmlContent);
            
            sendEmail(details);
            return new EmailResponseDto(true, "Verification email sent successfully to " + user.getEmail());

        } catch (IOException e) {
            logger.error("Error reading email template: {}", e.getMessage(), e);
            return new EmailResponseDto(false, "Error reading email template: " + e.getMessage());
        }
    }

    /**
     * Envía un ticket de soporte al buzón de atención técnica de IFit.
     *
     * @param userName nombre del usuario que envía el ticket.
     * @param userEmail email del usuario (puede ser "no-disponible" si el claim JWT no está mapeado).
     * @param ticket DTO con asunto, categoría y mensaje.
     * @return {@link EmailResponseDto} indicando si el envío fue exitoso.
     */
    public EmailResponseDto sendSupportTicketEmail(String userName, String userEmail, SupportTicketRequestDto ticket) {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("userName cannot be null or blank");
        }
        if (userEmail == null || userEmail.isBlank()) {
            throw new IllegalArgumentException("userEmail cannot be null or blank");
        }
        if (ticket == null || ticket.getMessage() == null || ticket.getMessage().isBlank()) {
            throw new IllegalArgumentException("ticket message cannot be null or blank");
        }

        try {
            Resource resource = new ClassPathResource("templates/email/supportticket.html");
            String htmlTemplate = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
            );

            String submittedAt = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            String htmlContent = htmlTemplate
                .replace("{{userName}}", userName)
                .replace("{{userEmail}}", userEmail)
                .replace("{{category}}", ticket.getCategory() != null ? ticket.getCategory() : "OTRO")
                .replace("{{subject}}", ticket.getSubject())
                .replace("{{message}}", ticket.getMessage())
                .replace("{{submittedAt}}", submittedAt);

            AppEmailDetails details = new AppEmailDetails();
            details.setRecipient(supportEmail);
            details.setSubject("[IFit Soporte] " + ticket.getSubject());
            details.setMsgBody(htmlContent);

            sendEmail(details);
            return new EmailResponseDto(true, "Ticket de soporte enviado correctamente.");

        } catch (IOException e) {
            logger.error("Error reading support ticket template: {}", e.getMessage(), e);
            return new EmailResponseDto(false, "Error al procesar la plantilla del ticket: " + e.getMessage());
        }
    }
}

