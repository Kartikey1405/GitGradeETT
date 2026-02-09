package com.gitgrade.GitGradeSpring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // ✅ Vital: Prevents crashing if AI adds extra fields
public class AnalysisResult {
    private RepoDetails details;
    private int score;
    private String summary;
    private List<RoadmapItem> roadmap;
    private TechStack tech_stack;
    private List<String> file_structure;

    // ✅ NEW FIELD: Used to catch the AI's bonus value internally
    private int quality_bonus;

    // --- Nested Static Classes for Structure ---

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RepoDetails {
        private String name;
        private String owner;
        private String description;
        private int stars;
        private int forks;
        private int open_issues;
        private String language;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoadmapItem {
        private String title;
        private String description;
        private String category;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TechStack {
        private List<String> frontend;
        private List<String> backend;
        private List<String> infrastructure;
    }
}