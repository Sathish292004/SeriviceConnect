package com.serviceconnect.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.serviceconnect.payment.dto.request.CreatePaymentRequest;
import com.serviceconnect.payment.dto.request.VerifyPaymentRequest;
import com.serviceconnect.payment.dto.response.CreatePaymentResponse;
import com.serviceconnect.payment.dto.response.VerifyPaymentResponse;
import com.serviceconnect.payment.entity.Payment;
import com.serviceconnect.payment.entity.PaymentStatus;
import com.serviceconnect.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;


    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest request) {

        /*
         * Temporary amount.
         *
         * Later:
         * Booking Service will provide the actual booking amount.
         */
        BigDecimal amount = new BigDecimal("500.00");

        try {

            // -------------------------------------------------
            // 1. Create Razorpay Order Request
            // -------------------------------------------------

            JSONObject orderRequest = new JSONObject();

            // Razorpay expects amount in paise.
            // Example:
            // ₹500.00 × 100 = 50000 paise
            orderRequest.put(
                    "amount",
                    amount
                            .multiply(BigDecimal.valueOf(100))
                            .longValue()
            );

            orderRequest.put(
                    "currency",
                    "INR"
            );

            orderRequest.put(
                    "receipt",
                    request.bookingId().toString()
            );


            // -------------------------------------------------
            // 2. Create Order in Razorpay
            // -------------------------------------------------

            Order razorpayOrder =
                    razorpayClient.orders.create(orderRequest);


            // -------------------------------------------------
            // 3. Save Payment in Our Database
            // -------------------------------------------------

            Payment payment = Payment.builder()
                    .id(UUID.randomUUID())
                    .bookingId(request.bookingId())
                    .userId(request.userId())
                    .amount(amount)
                    .currency("INR")
                    .status(PaymentStatus.PENDING)
                    .razorpayOrderId(
                            razorpayOrder.get("id")
                    )
                    .build();

            Payment savedPayment =
                    paymentRepository.save(payment);


            // -------------------------------------------------
            // 4. Return Payment Information
            // -------------------------------------------------

            return new CreatePaymentResponse(
                    savedPayment.getId(),
                    savedPayment.getBookingId(),
                    savedPayment.getAmount(),
                    savedPayment.getCurrency(),
                    savedPayment.getStatus().name(),
                    savedPayment.getRazorpayOrderId()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to create Razorpay order",
                    e
            );
        }
    }


    // =========================================================
    // VERIFY PAYMENT
    // =========================================================

    public VerifyPaymentResponse verifyPayment(
            VerifyPaymentRequest request) {

        try {

            // -------------------------------------------------
            // 1. Find Payment Using Razorpay Order ID
            // -------------------------------------------------

            Payment payment =
                    paymentRepository
                            .findByRazorpayOrderId(
                                    request.razorpayOrderId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Payment not found"
                                    )
                            );


            // -------------------------------------------------
            // 2. Create Signature Payload
            // -------------------------------------------------

            /*
             * Razorpay signature verification format:
             *
             * razorpayOrderId|razorpayPaymentId
             */

            String payload =
                    request.razorpayOrderId()
                            + "|"
                            + request.razorpayPaymentId();


            // -------------------------------------------------
            // 3. Verify Razorpay Signature
            // -------------------------------------------------

            boolean valid =
                    Utils.verifySignature(
                            payload,
                            request.razorpaySignature(),
                            razorpayKeySecret
                    );


            // -------------------------------------------------
            // 4. Invalid Signature
            // -------------------------------------------------

            if (!valid) {

                payment.setStatus(
                        PaymentStatus.FAILED
                );

                paymentRepository.save(payment);

                throw new RuntimeException(
                        "Invalid Razorpay signature"
                );
            }


            // -------------------------------------------------
            // 5. Payment Successful
            // -------------------------------------------------

            payment.setRazorpayPaymentId(
                    request.razorpayPaymentId()
            );

            payment.setRazorpaySignature(
                    request.razorpaySignature()
            );

            payment.setStatus(
                    PaymentStatus.SUCCESS
            );


            // -------------------------------------------------
            // 6. Save Updated Payment
            // -------------------------------------------------

            Payment savedPayment =
                    paymentRepository.save(payment);


            // -------------------------------------------------
            // 7. Return Verification Response
            // -------------------------------------------------

            return new VerifyPaymentResponse(
                    savedPayment.getId(),
                    savedPayment.getStatus().name(),
                    savedPayment.getRazorpayOrderId(),
                    savedPayment.getRazorpayPaymentId()
            );

        } catch (RuntimeException e) {

            // Don't hide our own meaningful errors.
            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Payment verification failed",
                    e
            );
        }
    }
}