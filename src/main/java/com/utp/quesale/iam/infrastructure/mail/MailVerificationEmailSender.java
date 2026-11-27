package com.utp.quesale.iam.infrastructure.mail;

import com.utp.quesale.iam.domain.exceptions.VerificationEmailDeliveryException;
import com.utp.quesale.iam.domain.services.VerificationEmailSender;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailVerificationEmailSender implements VerificationEmailSender {

    private static final Logger log = LoggerFactory.getLogger(MailVerificationEmailSender.class);
    private static final String SUBJECT = "Verifica tu correo en ¿Qué sale?";

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public MailVerificationEmailSender(ObjectProvider<JavaMailSender> mailSender,
                                       @Value("${app.mail.from:no-reply@quesale.local}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(String toEmail, String recipientName, String verificationLink) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("Correo de verificación para {} (SMTP no configurado): {}", toEmail, verificationLink);
            return;
        }
        try {
            sender.send(EmailLayout.build(sender, from, toEmail, SUBJECT, recipientName,
                    "Confirma que este correo es tuyo para terminar de crear tu cuenta.",
                    "Confirmar mi correo", verificationLink,
                    "Si no creaste una cuenta en ¿Qué sale?, ignora este mensaje."));
        } catch (MailException | MessagingException ex) {
            log.warn("Falló el envío del correo de verificación a {}: {}", toEmail, ex.getMessage());
            throw new VerificationEmailDeliveryException();
        }
    }
}
