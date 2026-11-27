package com.utp.quesale.iam.infrastructure;

import com.utp.quesale.iam.domain.exceptions.VerificationEmailDeliveryException;
import com.utp.quesale.iam.infrastructure.mail.MailVerificationEmailSender;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

    private JavaMailSender smtp() {
        JavaMailSender mail = mock(JavaMailSender.class);
        when(mail.createMimeMessage()).thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        return mail;
    }

    private static List<String> parts(Part part) throws Exception {
        List<String> found = new ArrayList<>();
        if (part.getContent() instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart child = multipart.getBodyPart(i);
                found.addAll(parts(child));
            }
        } else if (part.isMimeType("text/*")) {
            found.add(part.getContentType().toLowerCase() + "\n" + part.getContent());
        }
        return found;
    }

    @Test
    @DisplayName("Sin SMTP configurado no falla y solo deja el enlace en el registro")
    void withoutSmtpItOnlyLogs() {
        MailVerificationEmailSender sender = new MailVerificationEmailSender(providerOf(null), "no-reply@quesale.pe");

        assertDoesNotThrow(() -> sender.send("camila@utp.edu.pe", "Camila", LINK));
    }

    @Test
    @DisplayName("Con SMTP envía texto y HTML con un botón que abre el enlace")
    void sendsHtmlWithClickableButton() throws Exception {
        JavaMailSender mail = smtp();
        MailVerificationEmailSender sender = new MailVerificationEmailSender(providerOf(mail), "no-reply@quesale.pe");

        sender.send("camila@utp.edu.pe", "Camila", LINK);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mail).send(captor.capture());
        MimeMessage message = captor.getValue();
        message.saveChanges();
        assertEquals("no-reply@quesale.pe", ((InternetAddress) message.getFrom()[0]).getAddress());
        assertEquals("camila@utp.edu.pe", ((InternetAddress) message.getAllRecipients()[0]).getAddress());
        assertNotNull(message.getSubject());

        List<String> bodies = parts(message);
        String plain = bodies.stream().filter(b -> b.startsWith("text/plain")).findFirst().orElseThrow();
        String html = bodies.stream().filter(b -> b.startsWith("text/html")).findFirst().orElseThrow();
        assertTrue(plain.contains(LINK));
        assertTrue(plain.contains("Camila"));
        assertTrue(html.contains("href=\"" + LINK + "\""));
        assertTrue(html.contains("Confirmar mi correo"));
    }

    @Test
    @DisplayName("El nombre del destinatario se escapa en el HTML")
    void escapesRecipientNameInHtml() throws Exception {
        JavaMailSender mail = smtp();
        MailVerificationEmailSender sender = new MailVerificationEmailSender(providerOf(mail), "no-reply@quesale.pe");

        sender.send("camila@utp.edu.pe", "<b>Camila</b>", LINK);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mail).send(captor.capture());
        captor.getValue().saveChanges();
        String html = parts(captor.getValue()).stream().filter(b -> b.startsWith("text/html")).findFirst().orElseThrow();
        assertFalse(html.contains("<b>Camila</b>"));
        assertTrue(html.contains("&lt;b&gt;Camila&lt;/b&gt;"));
    }

    @Test
    @DisplayName("Si el servidor SMTP falla se informa con una excepción de dominio")
    void smtpFailureBecomesDeliveryException() {
        JavaMailSender mail = smtp();
        doThrow(new MailSendException("sin conexión")).when(mail).send(any(MimeMessage.class));
        MailVerificationEmailSender sender = new MailVerificationEmailSender(providerOf(mail), "no-reply@quesale.pe");

        assertThrows(VerificationEmailDeliveryException.class,
                () -> sender.send("camila@utp.edu.pe", "Camila", LINK));
    }
}
