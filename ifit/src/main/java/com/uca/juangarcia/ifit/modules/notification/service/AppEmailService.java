package com.uca.juangarcia.ifit.modules.notification.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.uca.juangarcia.ifit.exception.EmailNotFoundException;
import com.uca.juangarcia.ifit.modules.notification.dto.EmailResponseDto;
import com.uca.juangarcia.ifit.modules.notification.model.AppEmailDetails;
import com.uca.juangarcia.ifit.modules.user.mapper.AppUserMapper;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;
import com.uca.juangarcia.ifit.modules.user.service.AppUserService;

import jakarta.mail.internet.MimeMessage;

@Service
public class AppEmailService {

    private static final Logger logger = LoggerFactory.getLogger(AppEmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AppUserMapper userMapper;

    @Value("${spring.mail.username}") 
    private String sender;

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
    public EmailResponseDto sendVerificationEmail(String email) throws EmailNotFoundException {
        try {
            AppUser user = userMapper.toEntity(appUserService.findUserByEmail(email));
            if (user == null) {
                throw new EmailNotFoundException(email);
            }
            AppEmailDetails details = new AppEmailDetails();
            
            details.setRecipient(email);
            details.setSubject("Email Verification");

            String htmlTemplate = new String(Files.readAllBytes(Paths.get("src/main/resources/templates/email/verificationmail.html")), StandardCharsets.UTF_8);
            String htmlContent = htmlTemplate.replace("{{verificationCode}}", user.getVerificationCode());
            details.setMsgBody(htmlContent);
            
            sendEmail(details);
            return new EmailResponseDto(true, "Verification email sent successfully to " + email);

        } catch (IOException e) {
            logger.error("Error reading email template: {}", e.getMessage(), e);
            return new EmailResponseDto(false, "Error reading email template: " + e.getMessage());
        }
    }
}    

