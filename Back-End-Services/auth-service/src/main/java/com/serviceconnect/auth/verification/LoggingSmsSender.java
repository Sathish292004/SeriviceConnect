package com.serviceconnect.auth.verification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("local-logging")
public class LoggingSmsSender implements SmsSender {

    @Override
    public void sendVerificationCode(
            String phone,
            String code
    ) {

        log.info(
                "SMS VERIFICATION CODE | phone={} | code={}",
                phone,
                code
        );
    }
}