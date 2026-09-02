package com.serviceconnect.auth.verification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("local-logging")
public class LoggingEmailSender implements EmailSender {

    @Override
    public void sendVerificationCode(
            String email,
            String code
    ) {

        log.info(
                "EMAIL VERIFICATION CODE | email={} | code={}",
                email,
                code
        );
    }
}