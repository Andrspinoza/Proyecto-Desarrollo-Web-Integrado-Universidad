package com.utp.quesale.iam.infrastructure.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.util.HtmlUtils;

final class EmailLayout {

    private static final String BRAND = "#b84100";
    private static final String INK = "#172b4d";

    private EmailLayout() {
    }

    static MimeMessage build(JavaMailSender sender, String from, String to, String subject,
                             String recipientName, String intro, String buttonLabel, String link, String note)
            throws MessagingException {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text(recipientName, intro, link, note), html(recipientName, intro, buttonLabel, link, note));
        return message;
    }

    static String text(String recipientName, String intro, String link, String note) {
        return "Hola " + recipientName + ",\n\n" + intro + "\n" + link + "\n\n" + note + "\n";
    }

    static String html(String recipientName, String intro, String buttonLabel, String link, String note) {
        String name = HtmlUtils.htmlEscape(recipientName);
        String href = HtmlUtils.htmlEscape(link);
        return """
                <!doctype html>
                <html lang="es">
                <body style="margin:0;padding:0;background:#f4f5f7;font-family:Arial,Helvetica,sans-serif;color:%2$s;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f5f7;padding:24px 0;">
                <tr><td align="center">
                <table role="presentation" width="520" cellpadding="0" cellspacing="0" style="max-width:520px;width:100%%;background:#ffffff;border-radius:16px;padding:32px;">
                <tr><td style="font-size:22px;font-weight:bold;color:%1$s;padding-bottom:20px;">¿Qué sale?</td></tr>
                <tr><td style="font-size:16px;line-height:24px;padding-bottom:8px;">Hola %3$s,</td></tr>
                <tr><td style="font-size:16px;line-height:24px;padding-bottom:24px;">%4$s</td></tr>
                <tr><td align="center" style="padding-bottom:24px;">
                <a href="%6$s" target="_blank" style="display:inline-block;background:%1$s;color:#ffffff;text-decoration:none;font-weight:bold;font-size:16px;padding:14px 28px;border-radius:12px;">%5$s</a>
                </td></tr>
                <tr><td style="font-size:13px;line-height:20px;color:#5e6c84;padding-bottom:8px;">Si el botón no funciona, copia este enlace en tu navegador:</td></tr>
                <tr><td style="font-size:13px;line-height:20px;word-break:break-all;padding-bottom:24px;"><a href="%6$s" target="_blank" style="color:%1$s;">%6$s</a></td></tr>
                <tr><td style="font-size:13px;line-height:20px;color:#5e6c84;border-top:1px solid #e3e6ea;padding-top:16px;">%7$s</td></tr>
                </table>
                </td></tr>
                </table>
                </body>
                </html>
                """.formatted(BRAND, INK, name, HtmlUtils.htmlEscape(intro), HtmlUtils.htmlEscape(buttonLabel), href,
                HtmlUtils.htmlEscape(note));
    }
}
