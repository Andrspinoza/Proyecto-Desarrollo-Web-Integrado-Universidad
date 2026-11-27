package com.utp.quesale.iam.infrastructure.mail;

import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetEmailSender {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetEmailSender.class);
    private static final String SUBJECT = "Restablece tu contraseña en ¿Qué sale?";

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public PasswordResetEmailSender(ObjectProvider<JavaMailSender> mailSender,
                                    @Value("${app.mail.from:no-reply@quesale.local}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void send(String toEmail, String recipientName, String resetLink) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("Correo para restablecer la contraseña de {} (SMTP no configurado): {}", toEmail, resetLink);
            return;
        }
        try {
            sender.send(EmailLayout.build(sender, from, toEmail, SUBJECT, recipientName,
                    "Recibimos una solicitud para restablecer tu contraseña. El enlace vence en 1 hora.",
                    "Elegir nueva contraseña", resetLink,
                    "Si no la pediste tú, ignora este mensaje: tu contraseña sigue igual."));
        } catch (MailException | MessagingException ex) {
            log.warn("Falló el envío del correo de restablecimiento a {}: {}", toEmail, ex.getMessage());
            throw new IllegalStateException("Password reset email could not be delivered", ex);
        }
    }
}
