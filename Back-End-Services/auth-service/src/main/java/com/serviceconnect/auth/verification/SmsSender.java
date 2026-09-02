package com.serviceconnect.auth.verification;

public interface SmsSender {

    void sendVerificationCode(
            String phone,
            String code
    );
}