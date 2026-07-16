package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.config.ApplicationProperties;
import com.ecommerce.ecommerce_backend.config.EmailProperties;
import com.ecommerce.ecommerce_backend.exception.EmailFailureException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String VERIFICATION_EMAIL_SUBJECT =
            "Verify your email to activate your account.";

    private static final String PASSWORD_RESET_EMAIL_SUBJECT =
            "Your password reset request link.";

    private final JavaMailSender javaMailSender;
    private final EmailProperties emailProperties;
    private final ApplicationProperties applicationProperties;

    public void sendVerificationEmail(
            LocalUser user,
            String token
    ) throws EmailFailureException {

        String verificationUrl =
                buildFrontendUrl(
                        "/auth/verify",
                        token
                );

        String messageBody =
                "Please follow the link below to verify "
                        + "your email and activate your account.\n"
                        + verificationUrl;

        sendEmail(
                user.getEmail(),
                VERIFICATION_EMAIL_SUBJECT,
                messageBody
        );
    }

    public void sendPasswordResetEmail(
            LocalUser user,
            String token
    ) throws EmailFailureException {

        String passwordResetUrl =
                buildFrontendUrl(
                        "/auth/reset",
                        token
                );

        String messageBody =
                "You requested a password reset on our website. "
                        + "Please follow the link below to reset "
                        + "your password.\n"
                        + passwordResetUrl;

        sendEmail(
                user.getEmail(),
                PASSWORD_RESET_EMAIL_SUBJECT,
                messageBody
        );
    }

    private void sendEmail(
            String recipient,
            String subject,
            String messageBody
    ) throws EmailFailureException {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(
                emailProperties.from()
        );

        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(messageBody);

        try {
            javaMailSender.send(message);

        } catch (MailException ex) {
            throw new EmailFailureException();
        }
    }

    private String buildFrontendUrl(
            String path,
            String token
    ) {

        return UriComponentsBuilder
                .fromUri(
                        applicationProperties
                                .frontend()
                                .url()
                )
                .path(path)
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();
    }
}