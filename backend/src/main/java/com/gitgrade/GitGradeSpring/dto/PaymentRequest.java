package com.gitgrade.GitGradeSpring.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private double amount;
    private String message;
}