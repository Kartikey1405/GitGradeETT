package com.gitgrade.GitGradeSpring.dto;

import lombok.Data;

@Data
public class SendReportRequest {
    private AnalysisResult analysis_data; // Reuses the huge DTO we made earlier
}