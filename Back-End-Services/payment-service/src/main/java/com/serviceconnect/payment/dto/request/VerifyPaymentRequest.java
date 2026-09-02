package com.serviceconnect.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyPaymentRequest(

        @NotBlank
        String razorpayOrderId,

        @NotBlank
        String razorpayPaymentId,

        @NotBlank
        String razorpaySignature
) {
}