



package com.gitgrade.GitGradeSpring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitgrade.GitGradeSpring.dto.AnalysisResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AIService {

    @Value("${app.gemini.key}")
    private String geminiKey;

    private final WebClient webClient = WebClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Priority Files (Matches your Python list exactly)
    private static final Set<String> PRIORITY_FILES = Set.of(
            "package.json", "pom.xml", "build.gradle", "requirements.txt",
            "Dockerfile", "docker-compose.yml", "vite.config", "next.config",
            "tsconfig.json", "go.mod", "Cargo.toml", "App.tsx", "main.py",
            "tailwind.config.js", ".github/workflows"
    );

    public AnalysisResult analyzeCodeQuality(String readmeContent, List<String> files, int baseScore) {

        // ✅ CORRECTED MODEL: Using the specific 2.5 Lite model found in your logs
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + geminiKey;

        // 1. Python Logic Port: File Filtering
        String allFilesStr = String.join(" ", files);
        boolean isMonorepo = allFilesStr.contains("backend/") && allFilesStr.contains("frontend/");
        boolean hasGitignore = allFilesStr.contains(".gitignore");

        List<String> importantFiles = files.stream()
                .filter(f -> isPriorityMatch(f) || countOccurrences(f, '/') <= 2)
                .limit(80)
                .collect(Collectors.toList());
        String fileSummary = String.join("\n", importantFiles);

        // 2. Python Logic Port: Truncate Readme
        String readmeSnippet = (readmeContent != null && readmeContent.length() > 1000)
                ? readmeContent.substring(0, 1000)
                : (readmeContent != null ? readmeContent : "No README detected.");

        // 3. Python Logic Port: The "Harsh Architect" Prompt
        String promptText = String.format("""
            You are a harsh but helpful Senior Software Architect. 
            Analyze this GitHub repository structure and return a raw JSON response.
            
            CONTEXT:
            - Project Type: %s
            - Base Logic Score: %d/100
            - Has .gitignore: %s (Do NOT suggest adding it)
            
            FILES DETECTED (Truncated):
            %s
            
            README EXCERPT:
            %s
            
            INSTRUCTIONS:
            1. **Deep Tech Stack Detection:** Don't just say "JavaScript". Detect specific frameworks (e.g., "React", "Spring Boot", "Tailwind", "Vite").
            2. **Detailed Roadmap:** Provide **5 to 7** distinct, high-impact improvements.
            3. **Structure:** Each roadmap item MUST have a short 'title' and a detailed 'description' explaining HOW to do it.
            4. **Summary:** Write a concise executive summary. **IMPORTANT: Do NOT mention the specific numeric 'Logic Score' (e.g. %d) in the text summary.** Just focus on the code quality description.
            
            OUTPUT FORMAT (JSON ONLY - NO MARKDOWN):
            {
                "tech_stack": {
                    "frontend": ["List", "frontend", "techs"],
                    "backend": ["List", "backend", "techs"],
                    "infrastructure": ["Docker", "AWS", "etc"]
                },
                "summary": "A 3-4 sentence executive summary...",
                "roadmap": [
                    { "title": "Title", "description": "Desc", "category": "Arch" }
                ],
                "quality_bonus": 0
            }
            """,
                isMonorepo ? "Full-Stack Monorepo" : "Standard Repository",
                baseScore,
                hasGitignore ? "YES" : "NO",
                fileSummary,
                readmeSnippet.replace("\"", "'").replace("\n", " "),
                baseScore
        );

        Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", promptText)))));

        try {
            JsonNode response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("candidates")) {
                String rawText = response.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
                String cleanJson = rawText.replaceAll("```json", "").replaceAll("```", "").trim();

                // Map the JSON directly to your DTO
                return objectMapper.readValue(cleanJson, AnalysisResult.class);
            }

        } catch (WebClientResponseException e) {
            System.err.println(" AI API Failed. Code: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println(" AI Internal Error: " + e.getMessage());
        }

        return fallbackResult(baseScore);
    }

    private boolean isPriorityMatch(String path) {
        for (String key : PRIORITY_FILES) { if (path.contains(key)) return true; }
        return false;
    }

    private long countOccurrences(String str, char ch) {
        return str.chars().filter(c -> c == ch).count();
    }

    private AnalysisResult fallbackResult(int baseScore) {
        AnalysisResult fallback = new AnalysisResult();
        fallback.setScore(baseScore);
        fallback.setSummary("AI Analysis Unavailable. Using calculated metrics.");
        fallback.setRoadmap(List.of(new AnalysisResult.RoadmapItem("Check System", "AI Service unavailable.", "System")));
        fallback.setTech_stack(new AnalysisResult.TechStack(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        return fallback;
    }
}
