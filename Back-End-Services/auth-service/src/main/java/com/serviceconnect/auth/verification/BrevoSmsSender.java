package com.serviceconnect.auth.verification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class BrevoSmsSender implements SmsSender {

    private final RestClient restClient;

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sms-api-url}")
    private String apiUrl;

    @Value("${brevo.sms-sender}")
    private String sender;

    public BrevoSmsSender() {
        this.restClient = RestClient.create();
    }

    @Override
    public void sendVerificationCode(String phone, String code) {

        String recipient = phone.startsWith("+")
                ? phone
                : "+91" + phone;

        Map<String, Object> requestBody = Map.of(
                "sender", sender,
                "recipient", recipient,
                "content",
                "Your ServiceConnect verification code is: "
                        + code
                        + ". This code will expire soon.",
                "type", "transactional"
        );

        restClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("api-key", apiKey)
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();
    }
}