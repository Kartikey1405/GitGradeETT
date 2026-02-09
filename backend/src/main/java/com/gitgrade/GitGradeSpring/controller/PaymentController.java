package com.gitgrade.GitGradeSpring.controller;

import com.gitgrade.GitGradeSpring.dto.PaymentLinkResponse;
import com.gitgrade.GitGradeSpring.dto.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${app.payment.upi}")
    private String upiId;

    @PostMapping("/generate-link")
    public PaymentLinkResponse generatePaymentLink(@RequestBody PaymentRequest request) {
        String txnId = UUID.randomUUID().toString();

        try {
            // Encode parameters strictly for UPI standard
            String params = String.format("pa=%s&pn=%s&tn=%s&am=%s&cu=INR",
                    URLEncoder.encode(upiId, StandardCharsets.UTF_8),
                    URLEncoder.encode("GitGrade Support", StandardCharsets.UTF_8),
                    URLEncoder.encode(request.getMessage(), StandardCharsets.UTF_8),
                    request.getAmount()
            );

            String upiLink = "upi://pay?" + params;

            return new PaymentLinkResponse(upiLink, txnId);

        } catch (Exception e) {
            throw new RuntimeException("Error encoding UPI link");
        }
    }
}