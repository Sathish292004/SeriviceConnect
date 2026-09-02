package com.serviceconnect.payment.controller;


import com.serviceconnect.payment.dto.request.CreatePaymentRequest;
import com.serviceconnect.payment.dto.request.VerifyPaymentRequest;
import com.serviceconnect.payment.dto.response.CreatePaymentResponse;
import com.serviceconnect.payment.dto.response.VerifyPaymentResponse;
import com.serviceconnect.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        return ResponseEntity.ok(
                paymentService.createPayment(request)
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {

        return ResponseEntity.ok(
                paymentService.verifyPayment(request)
        );
    }
}