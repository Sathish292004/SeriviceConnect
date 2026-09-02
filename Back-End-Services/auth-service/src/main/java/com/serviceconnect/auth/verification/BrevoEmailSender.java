package com.serviceconnect.auth.verification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class BrevoEmailSender implements EmailSender {

    private final RestClient restClient;

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.api-url}")
    private String apiUrl;

    @Value("${brevo.sender.name}")
    private String senderName;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    public BrevoEmailSender() {
        this.restClient = RestClient.create();
    }

    @Override
    public void sendVerificationCode(String email, String code) {

        Map<String, Object> requestBody = Map.of(
                "sender", Map.of(
                        "name", senderName,
                        "email", senderEmail
                ),
                "to", List.of(
                        Map.of(
                                "email", email
                        )
                ),
                "subject", "ServiceConnect Email Verification",
                "textContent",
                "Your ServiceConnect verification code is: "
                        + code
                        + "\n\nThis code will expire soon."
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