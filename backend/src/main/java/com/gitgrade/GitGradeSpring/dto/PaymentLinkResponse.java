package com.gitgrade.GitGradeSpring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentLinkResponse {
    private String payment_url;
    private String transaction_id;
}