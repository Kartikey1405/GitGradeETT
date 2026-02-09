//package com.gitgrade.GitGradeSpring.controller;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.gitgrade.GitGradeSpring.dto.AnalysisResult;
//import com.gitgrade.GitGradeSpring.dto.AnalyzeRequest;
//import com.gitgrade.GitGradeSpring.dto.RepoData;
//import com.gitgrade.GitGradeSpring.dto.SendReportRequest;
//import com.gitgrade.GitGradeSpring.entity.Analysis;
//import com.gitgrade.GitGradeSpring.entity.User;
//import com.gitgrade.GitGradeSpring.repository.AnalysisRepository;
//import com.gitgrade.GitGradeSpring.repository.UserRepository;
//import com.gitgrade.GitGradeSpring.service.AIService;
//import com.gitgrade.GitGradeSpring.service.EmailService;
//import com.gitgrade.GitGradeSpring.service.GitHubService;
//import com.gitgrade.GitGradeSpring.service.ScoringService;
//import com.gitgrade.GitGradeSpring.util.JwtUtil;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/analyze")
//public class AnalyzeController {
//
//    @Autowired private GitHubService gitHubService;
//    @Autowired private ScoringService scoringService;
//    @Autowired private AIService aiService;
//    @Autowired private EmailService emailService; // Added Email Service
//    @Autowired private UserRepository userRepository;
//    @Autowired private AnalysisRepository analysisRepository;
//    @Autowired private JwtUtil jwtUtil;
//
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    @PostMapping("/")
//    public ResponseEntity<?> analyzeRepo(
//            @RequestBody AnalyzeRequest request,
//            @RequestHeader(value = "Authorization", required = false) String authHeader
//    ) {
//        try {
//            // 1. Validate URL
//            String[] parts = request.getGithub_url().split("/");
//            if (parts.length < 2) return ResponseEntity.badRequest().body("Invalid GitHub URL");
//
//            String owner = parts[parts.length - 2];
//            String repoName = parts[parts.length - 1];
//
//            // 2. Fetch Data (GitHub)
//            RepoData repoData = gitHubService.fetchRepoData(owner, repoName);
//
//            // 3. Calculate Base Score (Algorithm)
//            int baseScore = scoringService.calculateScore(repoData.getMetadata(), repoData.getFiles(), repoData.getReadmeContent());
//
//            // 4. Get AI Analysis (Gemini)
//            JsonNode aiResult = aiService.analyzeCodeQuality(repoData.getReadmeContent(), repoData.getFiles(), baseScore);
//
//            // 5. Calculate Final Score
//            int qualityBonus = aiResult.has("quality_bonus") ? aiResult.get("quality_bonus").asInt() : 0;
//            int finalScore = Math.min(100, Math.max(0, baseScore + qualityBonus));
//
//            // 6. Build Result Object
//            AnalysisResult result = AnalysisResult.builder()
//                    .score(finalScore)
//                    .summary(aiResult.path("summary").asText("Analysis complete."))
//                    .file_structure(repoData.getFiles().subList(0, Math.min(50, repoData.getFiles().size()))) // Limit file list
//                    .details(new AnalysisResult.RepoDetails(
//                            repoData.getMetadata().path("name").asText(repoName),
//                            repoData.getMetadata().path("owner").path("login").asText(owner),
//                            repoData.getMetadata().path("description").asText("No description"),
//                            repoData.getMetadata().path("stargazers_count").asInt(0),
//                            repoData.getMetadata().path("forks_count").asInt(0),
//                            repoData.getMetadata().path("open_issues_count").asInt(0),
//                            repoData.getMetadata().path("language").asText("Unknown")
//                    ))
//                    .build();
//
//            // 7. Parse AI JSON into Java Objects (Roadmap & TechStack)
//            List<AnalysisResult.RoadmapItem> roadmap = new ArrayList<>();
//            if (aiResult.has("roadmap")) {
//                for (JsonNode item : aiResult.get("roadmap")) {
//                    roadmap.add(new AnalysisResult.RoadmapItem(
//                            item.path("title").asText(),
//                            item.path("description").asText(),
//                            item.path("category").asText("General")
//                    ));
//                }
//            }
//            result.setRoadmap(roadmap);
//
//            // 8. Save to DB (If User is Logged In)
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                saveAnalysisToDB(authHeader.substring(7), request.getGithub_url(), repoName, finalScore, aiResult);
//            }
//
//            return ResponseEntity.ok(result);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body("Analysis Failed: " + e.getMessage());
//        }
//    }
//
//    // --- NEW ENDPOINT: SEND REPORT VIA EMAIL ---
//    @PostMapping("/send-report")
//    public ResponseEntity<?> sendReport(
//            @RequestBody SendReportRequest request,
//            @RequestHeader(value = "Authorization", required = false) String authHeader
//    ) {
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return ResponseEntity.status(401).body("Missing or invalid token");
//        }
//
//        try {
//            // 1. Extract Email from Token
//            String token = authHeader.substring(7);
//            String userEmail = jwtUtil.extractEmail(token);
//
//            if (userEmail == null) return ResponseEntity.status(401).body("Invalid Token Payload");
//
//            System.out.println("Generating PDF for " + userEmail + "...");
//
//            // 2. Generate PDF
//            String pdfPath = emailService.generatePdf(request.getAnalysis_data());
//
//            // 3. Send Email
//            boolean success = emailService.sendEmail(userEmail, pdfPath);
//
//            if (success) {
//                return ResponseEntity.ok(Map.of("message", "Report sent successfully to " + userEmail));
//            } else {
//                return ResponseEntity.status(500).body("Failed to send email via SendGrid");
//            }
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("PDF Generation failed: " + e.getMessage());
//        }
//    }
//
//    private void saveAnalysisToDB(String token, String url, String name, int score, JsonNode aiResult) {
//        try {
//            String email = jwtUtil.extractEmail(token);
//            User user = userRepository.findByEmail(email).orElse(null);
//
//            if (user != null) {
//                Analysis analysis = new Analysis();
//                analysis.setOwner(user);
//                analysis.setGithubUrl(url);
//                analysis.setRepoName(name);
//                analysis.setOverallScore((double) score);
//                analysis.setSummary(aiResult.path("summary").asText());
//                analysis.setFullJsonResult(aiResult.toString()); // Save raw JSON
//
//                analysisRepository.save(analysis);
//                System.out.println("Saved analysis for user: " + email);
//            }
//        } catch (Exception e) {
//            System.err.println("DB Save Error: " + e.getMessage());
//        }
//    }
//}


package com.gitgrade.GitGradeSpring.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitgrade.GitGradeSpring.dto.AnalysisResult;
import com.gitgrade.GitGradeSpring.dto.AnalyzeRequest;
import com.gitgrade.GitGradeSpring.dto.RepoData;
import com.gitgrade.GitGradeSpring.dto.SendReportRequest;
import com.gitgrade.GitGradeSpring.entity.Analysis;
import com.gitgrade.GitGradeSpring.entity.User;
import com.gitgrade.GitGradeSpring.repository.AnalysisRepository;
import com.gitgrade.GitGradeSpring.repository.UserRepository;
import com.gitgrade.GitGradeSpring.service.AIService;
import com.gitgrade.GitGradeSpring.service.EmailService;
import com.gitgrade.GitGradeSpring.service.GitHubService;
import com.gitgrade.GitGradeSpring.service.ScoringService;
import com.gitgrade.GitGradeSpring.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analyze")
public class AnalyzeController {

    @Autowired private GitHubService gitHubService;
    @Autowired private ScoringService scoringService;
    @Autowired private AIService aiService;
    @Autowired private EmailService emailService;
    @Autowired private UserRepository userRepository;
    @Autowired private AnalysisRepository analysisRepository;
    @Autowired private JwtUtil jwtUtil;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/")
    public ResponseEntity<?> analyzeRepo(
            @RequestBody AnalyzeRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            // 1. Validate URL
            String[] parts = request.getGithub_url().split("/");
            if (parts.length < 2) return ResponseEntity.badRequest().body("Invalid GitHub URL");

            String owner = parts[parts.length - 2];
            String repoName = parts[parts.length - 1];

            // 2. Fetch Data (GitHub)
            RepoData repoData = gitHubService.fetchRepoData(owner, repoName);

            // 3. Calculate Base Score (Algorithm)
            int baseScore = scoringService.calculateScore(repoData.getMetadata(), repoData.getFiles(), repoData.getReadmeContent());

            // 4. Get AI Analysis (Gemini) - Now returns the Object directly
            AnalysisResult result = aiService.analyzeCodeQuality(repoData.getReadmeContent(), repoData.getFiles(), baseScore);

            // 5. Calculate Final Score (Base + AI Bonus)
            int finalScore = Math.min(100, Math.max(0, baseScore + result.getQuality_bonus()));
            result.setScore(finalScore);

            // 6. Fill in the missing GitHub Details (AI doesn't know these)
            result.setDetails(new AnalysisResult.RepoDetails(
                    repoData.getMetadata().path("name").asText(repoName),
                    repoData.getMetadata().path("owner").path("login").asText(owner),
                    repoData.getMetadata().path("description").asText("No description"),
                    repoData.getMetadata().path("stargazers_count").asInt(0),
                    repoData.getMetadata().path("forks_count").asInt(0),
                    repoData.getMetadata().path("open_issues_count").asInt(0),
                    repoData.getMetadata().path("language").asText("Unknown")
            ));

            // 7. Add File Structure (Truncated for UI performance)
            result.setFile_structure(repoData.getFiles().subList(0, Math.min(50, repoData.getFiles().size())));

            // 8. Save to DB (If User is Logged In)
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                saveAnalysisToDB(authHeader.substring(7), request.getGithub_url(), repoName, result);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Analysis Failed: " + e.getMessage());
        }
    }

    // --- ENDPOINT: SEND REPORT VIA EMAIL ---
    @PostMapping("/send-report")
    public ResponseEntity<?> sendReport(
            @RequestBody SendReportRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid token");
        }

        try {
            String token = authHeader.substring(7);
            String userEmail = jwtUtil.extractEmail(token);

            if (userEmail == null) return ResponseEntity.status(401).body("Invalid Token Payload");

            System.out.println("Generating PDF for " + userEmail + "...");
            String pdfPath = emailService.generatePdf(request.getAnalysis_data());
            boolean success = emailService.sendEmail(userEmail, pdfPath);

            if (success) {
                return ResponseEntity.ok(Map.of("message", "Report sent successfully to " + userEmail));
            } else {
                return ResponseEntity.status(500).body("Failed to send email via SendGrid");
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body("PDF Generation failed: " + e.getMessage());
        }
    }

    // Helper: Save Analysis to Database
    private void saveAnalysisToDB(String token, String url, String name, AnalysisResult result) {
        try {
            String email = jwtUtil.extractEmail(token);
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                Analysis analysis = new Analysis();
                analysis.setOwner(user);
                analysis.setGithubUrl(url);
                analysis.setRepoName(name);
                analysis.setOverallScore((double) result.getScore());
                analysis.setSummary(result.getSummary());

                // Convert the Java Object back to JSON string for storage
                try {
                    analysis.setFullJsonResult(mapper.writeValueAsString(result));
                } catch (JsonProcessingException e) {
                    analysis.setFullJsonResult("{}");
                    System.err.println("Failed to serialize result to JSON");
                }

                analysisRepository.save(analysis);
                System.out.println("Saved analysis for user: " + email);
            }
        } catch (Exception e) {
            System.err.println("DB Save Error: " + e.getMessage());
        }
    }
}