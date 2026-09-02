package com.serviceconnect.auth.verification;

public interface EmailSender {

    void sendVerificationCode(
            String email,
            String code
    );
}