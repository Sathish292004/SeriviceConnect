package com.serviceconnect.payment.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentResponse(

        UUID paymentId,
        UUID bookingId,
        BigDecimal amount,
        String currency,
        String status,
        String razorpayOrderId
) {
}