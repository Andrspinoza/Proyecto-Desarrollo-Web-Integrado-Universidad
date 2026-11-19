package com.utp.quesale.iam.infrastructure;

import com.utp.quesale.iam.domain.exceptions.VerificationEmailDeliveryException;
import com.utp.quesale.iam.infrastructure.mail.MailVerificationEmailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MailVerificationEmailSenderTest {

    private static final String LINK = "https://quesale.example/api/v1/email-verifications/confirm?token=abc";

    @SuppressWarnings("unchecked")
    private ObjectProvider<JavaMailSender> providerOf(JavaMailSender sender) {
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        return provider;
    }

    @Test
    @DisplayName("Sin SMTP configurado no falla y solo deja el enlace en el registro")
    void withoutSmtpItOnlyLogs() {
        MailVerificationEmailSender sender = new MailVerificationEmailSender(providerOf(null), "no-reply@quesale.pe");

        assertDoesNotThrow(() -> sender.send("camila@utp.edu.pe", "Camila", LINK));
    }

    @Test
    @DisplayName("Con SMTP envía el mensaje con remitente, destinatario y enlace")
    void sendsMessageThroughSmtp() {
        JavaMailSender mail = mock(JavaMailSender.class);
        MailVerificationEmailSender sender = new MailVerificationEmailSender(providerOf(mail), "no-reply@quesale.pe");

        sender.send("camila@utp.edu.pe", "Camila", LINK);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("no-reply@quesale.pe", message.getFrom());
        assertArrayEquals(new String[]{"camila@utp.edu.pe"}, message.getTo());
        assertNotNull(message.getSubject());
        assertTrue(message.getText().contains(LINK));
        assertTrue(message.getText().contains("Camila"));
    }

    @Test
    @DisplayName("Si el servidor SMTP falla se informa con una excepción de dominio")
    void smtpFailureBecomesDeliveryException() {
        JavaMailSender mail = mock(JavaMailSender.class);
        doThrow(new MailSendException("sin conexión")).when(mail).send(any(SimpleMailMessage.class));
        MailVerificationEmailSender sender = new MailVerificationEmailSender(providerOf(mail), "no-reply@quesale.pe");

        assertThrows(VerificationEmailDeliveryException.class,
                () -> sender.send("camila@utp.edu.pe", "Camila", LINK));
    }
}
