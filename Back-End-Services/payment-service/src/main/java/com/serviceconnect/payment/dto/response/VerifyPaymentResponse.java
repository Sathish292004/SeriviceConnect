package com.serviceconnect.payment.dto.response;

import java.util.UUID;

public record VerifyPaymentResponse(

        UUID paymentId,
        String status,
        String razorpayOrderId,
        String razorpayPaymentId
) {
}