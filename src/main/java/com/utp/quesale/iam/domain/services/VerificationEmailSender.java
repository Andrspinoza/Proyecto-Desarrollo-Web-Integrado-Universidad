package com.utp.quesale.iam.domain.services;

public interface VerificationEmailSender {

    void send(String toEmail, String recipientName, String verificationLink);
}
