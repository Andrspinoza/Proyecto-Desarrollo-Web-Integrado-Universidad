package com.utp.quesale.iam.application;

import com.utp.quesale.iam.domain.services.VerificationEmailSender;

import java.util.ArrayList;
import java.util.List;

public class RecordingVerificationEmailSender implements VerificationEmailSender {

    public record Sent(String toEmail, String recipientName, String link) {
        public String token() {
            return link.substring(link.indexOf("token=") + "token=".length());
        }
    }

    private final List<Sent> sent = new ArrayList<>();

    @Override
    public void send(String toEmail, String recipientName, String verificationLink) {
        sent.add(new Sent(toEmail, recipientName, verificationLink));
    }

    public List<Sent> sent() {
        return sent;
    }

    public Sent last() {
        return sent.get(sent.size() - 1);
    }
}
