package com.gitgrade.GitGradeSpring.dto;

import lombok.Data;

@Data
public class AnalyzeRequest {
    private String github_url; // Matches JSON key "github_url" from frontend
}