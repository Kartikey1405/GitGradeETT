//package com.gitgrade.GitGradeSpring.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class AIService {
//
//    @Value("${app.gemini.key}")
//    private String geminiKey;
//
//    private final WebClient webClient = WebClient.create();
//    private final ObjectMapper objectMapper = new ObjectMapper(); // For parsing JSON strings
//
//    public JsonNode analyzeCodeQuality(String readmeContent, List<String> files, int baseScore) {
//        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiKey;
//
//        // 1. Prepare Context Data
//        String allFiles = String.join("\n", files.subList(0, Math.min(files.size(), 80))); // Limit to 80 files
//        boolean isMonorepo = allFiles.contains("backend/") && allFiles.contains("frontend/");
//        String readmeSnippet = (readmeContent != null && readmeContent.length() > 1000)
//                ? readmeContent.substring(0, 1000)
//                : readmeContent;
//
//        // 2. Construct the Prompt
//        String promptText = String.format("""
//            You are a Senior Software Architect. Analyze this GitHub repository structure and return a raw JSON response.
//
//            CONTEXT:
//            - Project Type: %s
//            - Base Logic Score: %d/100
//
//            FILES DETECTED:
//            %s
//
//            README EXCERPT:
//            %s
//
//            INSTRUCTIONS:
//            1. Detect specific Tech Stack (Frontend, Backend, Infra).
//            2. Provide 5-7 high-impact roadmap improvements.
//            3. Write a concise executive summary (Do NOT mention the numeric score).
//
//            OUTPUT FORMAT (Strict JSON):
//            {
//                "tech_stack": { "frontend": [], "backend": [], "infrastructure": [] },
//                "summary": "...",
//                "roadmap": [ { "title": "...", "description": "...", "category": "..." } ],
//                "quality_bonus": 0
//            }
//            """,
//                isMonorepo ? "Monorepo" : "Standard",
//                baseScore,
//                allFiles,
//                readmeSnippet != null ? readmeSnippet.replace("\"", "'") : "No README"
//        );
//
//        // 3. Request Body for Gemini
//        Map<String, Object> requestBody = Map.of(
//                "contents", List.of(Map.of("parts", List.of(Map.of("text", promptText))))
//        );
//
//        try {
//            // 4. Call API
//            JsonNode response = webClient.post()
//                    .uri(url)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(JsonNode.class)
//                    .block();
//
//            // 5. Extract and Clean JSON from Response
//            String rawText = response.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
//
//            // Remove Markdown code blocks (```json ... ```)
//            String cleanJson = rawText.replace("```json", "").replace("```", "").strip();
//
//            return objectMapper.readTree(cleanJson);
//
//        } catch (Exception e) {
//            System.err.println("AI Error: " + e.getMessage());
//            // Return empty fallback JSON
//            try {
//                return objectMapper.readTree("{ \"summary\": \"AI Analysis Unavailable\", \"roadmap\": [], \"tech_stack\": {}, \"quality_bonus\": 0 }");
//            } catch (JsonProcessingException ex) {
//                return null;
//            }
//        }
//    }
//}

//package com.gitgrade.GitGradeSpring.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//public class AIService {
//
//    @Value("${app.gemini.key}")
//    private String geminiKey;
//
//    private final WebClient webClient = WebClient.create();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    // Priority files to look for (Ported from your Python code)
//    private static final Set<String> PRIORITY_FILES = Set.of(
//            "package.json", "pom.xml", "build.gradle", "requirements.txt",
//            "Dockerfile", "docker-compose.yml", "vite.config.js", "vite.config.ts",
//            "next.config.js", "tsconfig.json", "go.mod", "Cargo.toml",
//            "App.tsx", "main.py", "tailwind.config.js", ".github/workflows"
//    );
//
//    public JsonNode analyzeCodeQuality(String readmeContent, List<String> files, int baseScore) {
//        // ✅ UPDATED MODEL: gemini-1.5-flash (High Limit: 1,500 RPM)
//        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiKey;
//
//        // 1. Smart File Filtering (Match Python Logic)
//        String allFilesStr = String.join(" ", files);
//        boolean isMonorepo = allFilesStr.contains("backend/") && allFilesStr.contains("frontend/");
//        boolean hasGitignore = files.contains(".gitignore");
//
//        List<String> importantFiles = files.stream()
//                .filter(f -> isPriorityFile(f) || countOccurrences(f, '/') <= 2) // Priority or shallow depth
//                .limit(80) // Limit to 80 files like Python
//                .collect(Collectors.toList());
//
//        String fileSummary = String.join("\n", importantFiles);
//
//        // Truncate Readme
//        String readmeSnippet = (readmeContent != null && readmeContent.length() > 2000)
//                ? readmeContent.substring(0, 2000)
//                : (readmeContent != null ? readmeContent : "No README detected.");
//
//        // 2. Construct the "Senior Architect" Prompt
//        String promptText = String.format("""
//            You are a harsh but helpful Senior Software Architect.
//            Analyze this GitHub repository structure and return a raw JSON response.
//
//            CONTEXT:
//            - Project Type: %s
//            - Base Logic Score: %d/100
//            - Has .gitignore: %s (Do NOT suggest adding it)
//
//            FILES DETECTED (Truncated):
//            %s
//
//            README EXCERPT:
//            %s
//
//            INSTRUCTIONS:
//            1. **Deep Tech Stack Detection:** Don't just say "JavaScript". Detect specific frameworks (e.g., "React", "Spring Boot", "Tailwind", "Vite").
//            2. **Detailed Roadmap:** Provide **5 to 7** distinct, high-impact improvements.
//            3. **Structure:** Each roadmap item MUST have a short 'title' and a detailed 'description' explaining HOW to do it.
//            4. **Summary:** Write a concise executive summary. **IMPORTANT: Do NOT mention the specific numeric 'Logic Score' (e.g. %d) in the text summary.** Just focus on the code quality description.
//
//            OUTPUT FORMAT (JSON ONLY - NO MARKDOWN):
//            {
//                "tech_stack": { "frontend": [], "backend": [], "infrastructure": [] },
//                "summary": "A 3-4 sentence executive summary...",
//                "roadmap": [ { "title": "...", "description": "...", "category": "..." } ],
//                "quality_bonus": 0
//            }
//            """,
//                isMonorepo ? "Full-Stack Monorepo" : "Standard Repository",
//                baseScore,
//                hasGitignore ? "YES" : "NO",
//                fileSummary,
//                readmeSnippet.replace("\"", "'"), // Escape quotes to prevent JSON break
//                baseScore
//        );
//
//        // 3. Request Body
//        Map<String, Object> requestBody = Map.of(
//                "contents", List.of(Map.of("parts", List.of(Map.of("text", promptText))))
//        );
//
//        try {
//            // 4. Call API
//            JsonNode response = webClient.post()
//                    .uri(url)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(JsonNode.class)
//                    .block();
//
//            // 5. Clean & Parse JSON
//            if (response != null && response.has("candidates")) {
//                String rawText = response.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
//                String cleanJson = rawText.replaceAll("```json", "").replaceAll("```", "").trim();
//                return objectMapper.readTree(cleanJson);
//            }
//
//            return fallbackResponse();
//
//        } catch (Exception e) {
//            System.err.println("AI Error: " + e.getMessage());
//            return fallbackResponse();
//        }
//    }
//
//    // Helper: Check if file matches priority list
//    private boolean isPriorityFile(String path) {
//        for (String key : PRIORITY_FILES) {
//            if (path.contains(key)) return true;
//        }
//        return false;
//    }
//
//    // Helper: Count slashes for depth check
//    private long countOccurrences(String str, char ch) {
//        return str.chars().filter(c -> c == ch).count();
//    }
//
//    private JsonNode fallbackResponse() {
//        try {
//            return objectMapper.readTree("""
//                {
//                    "summary": "AI Analysis Unavailable (Rate Limit or Error). Showing raw analysis.",
//                    "roadmap": [
//                        { "title": "Check Connection", "description": "Could not connect to AI service.", "category": "System" }
//                    ],
//                    "tech_stack": { "frontend": [], "backend": [], "infrastructure": [] },
//                    "quality_bonus": 0
//                }
//                """);
//        } catch (JsonProcessingException e) {
//            return null;
//        }
//    }
//}




//package com.gitgrade.GitGradeSpring.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.gitgrade.GitGradeSpring.dto.AnalysisResult;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//public class AIService {
//
//    @Value("${app.gemini.key}")
//    private String geminiKey;
//
//    private final WebClient webClient = WebClient.create();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    // Priority files logic (same as before)
//    private static final Set<String> PRIORITY_FILES = Set.of(
//            "package.json", "pom.xml", "build.gradle", "requirements.txt",
//            "Dockerfile", "docker-compose.yml", "vite.config.js", "vite.config.ts",
//            "next.config.js", "tsconfig.json", "go.mod", "Cargo.toml",
//            "App.tsx", "main.py", "tailwind.config.js", ".github/workflows"
//    );
//
//    // ✅ CHANGED: Return AnalysisResult instead of JsonNode
//    public AnalysisResult analyzeCodeQuality(String readmeContent, List<String> files, int baseScore) {
//        // ✅ CORRECT MODEL: gemini-1.5-flash (1500 RPM limit)
//        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiKey;
//
//        // 1. Prepare Data
//        String allFilesStr = String.join(" ", files);
//        boolean isMonorepo = allFilesStr.contains("backend/") && allFilesStr.contains("frontend/");
//
//        List<String> importantFiles = files.stream()
//                .filter(f -> isPriorityFile(f) || countOccurrences(f, '/') <= 2)
//                .limit(80)
//                .collect(Collectors.toList());
//        String fileSummary = String.join("\n", importantFiles);
//
//        String readmeSnippet = (readmeContent != null && readmeContent.length() > 2000)
//                ? readmeContent.substring(0, 2000)
//                : (readmeContent != null ? readmeContent : "No README");
//
//        // 2. Prompt (Same as before, requesting strict JSON)
//        String promptText = String.format("""
//            You are a Senior Software Architect. Analyze this repo and return raw JSON.
//
//            CONTEXT:
//            - Type: %s
//            - Logic Score: %d/100
//
//            FILES (Truncated):
//            %s
//
//            README:
//            %s
//
//            INSTRUCTIONS:
//            1. Detect Tech Stack (Frontend, Backend, Infra).
//            2. Provide 5-7 Roadmap Improvements (title, description, category).
//            3. Write a concise Summary (Do NOT mention the numeric score %d).
//            4. Calculate a 'quality_bonus' (-10 to +10) based on best practices.
//
//            OUTPUT JSON ONLY:
//            {
//                "tech_stack": { "frontend": [], "backend": [], "infrastructure": [] },
//                "summary": "...",
//                "roadmap": [ { "title": "...", "description": "...", "category": "..." } ],
//                "quality_bonus": 0
//            }
//            """,
//                isMonorepo ? "Monorepo" : "Standard",
//                baseScore,
//                fileSummary,
//                readmeSnippet.replace("\"", "'"),
//                baseScore
//        );
//
//        // 3. Request
//        Map<String, Object> requestBody = Map.of(
//                "contents", List.of(Map.of("parts", List.of(Map.of("text", promptText))))
//        );
//
//        try {
//            JsonNode response = webClient.post()
//                    .uri(url)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(JsonNode.class)
//                    .block();
//
//            if (response != null && response.has("candidates")) {
//                String rawText = response.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
//                String cleanJson = rawText.replaceAll("```json", "").replaceAll("```", "").trim();
//
//                // ✅ MAP JSON TO DTO AUTOMATICALLY
//                AnalysisResult result = objectMapper.readValue(cleanJson, AnalysisResult.class);
//
//                // Apply the score calculation inside the service
//                // (Note: AnalysisResult doesn't have quality_bonus field in Java,
//                // so we parse it separately or add it to DTO. Assuming you add it to DTO for simplicity)
//                // If DTO doesn't have it, we just return the result and handle score logic in controller?
//                // Better: Let's assume we map strict fields.
//
//                return result;
//            }
//
//        } catch (WebClientResponseException e) {
//            System.err.println("❌ AI API Failed. Code: " + e.getStatusCode() + " Body: " + e.getResponseBodyAsString());
//        } catch (Exception e) {
//            System.err.println("❌ AI Internal Error: " + e.getMessage());
//        }
//
//        return fallbackResult(baseScore);
//    }
//
//    private boolean isPriorityFile(String path) {
//        for (String key : PRIORITY_FILES) {
//            if (path.contains(key)) return true;
//        }
//        return false;
//    }
//
//    private long countOccurrences(String str, char ch) {
//        return str.chars().filter(c -> c == ch).count();
//    }
//
//    private AnalysisResult fallbackResult(int baseScore) {
//        AnalysisResult fallback = new AnalysisResult();
//        fallback.setScore(baseScore);
//        fallback.setSummary("AI Analysis Unavailable. Using calculated metrics.");
//        fallback.setRoadmap(List.of(
//                new AnalysisResult.RoadmapItem("Check System", "AI Service could not be reached.", "System")
//        ));
//        fallback.setTech_stack(new AnalysisResult.TechStack(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
//        return fallback;
//    }
//}


//package com.gitgrade.GitGradeSpring.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.gitgrade.GitGradeSpring.dto.AnalysisResult;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//public class AIService {
//
//    @Value("${app.gemini.key}")
//    private String geminiKey;
//
//    private final WebClient webClient = WebClient.create();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    // Priority files (Smart Filter)
//    private static final Set<String> PRIORITY_FILES = Set.of(
//            "package.json", "pom.xml", "build.gradle", "requirements.txt",
//            "Dockerfile", "docker-compose.yml", "vite.config.js", "vite.config.ts",
//            "next.config.js", "tsconfig.json", "go.mod", "Cargo.toml",
//            "App.tsx", "main.py", "tailwind.config.js", ".github/workflows"
//    );
//
//    public AnalysisResult analyzeCodeQuality(String readmeContent, List<String> files, int baseScore) {
//
//        // ✅ FIXED URL: Trying 'gemini-1.5-flash-latest' which is the standard alias
//        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + geminiKey;
//
//        // 1. Prepare Data
//        String allFilesStr = String.join(" ", files);
//        boolean isMonorepo = allFilesStr.contains("backend/") && allFilesStr.contains("frontend/");
//
//        List<String> importantFiles = files.stream()
//                .filter(f -> isPriorityFile(f) || countOccurrences(f, '/') <= 2)
//                .limit(80)
//                .collect(Collectors.toList());
//        String fileSummary = String.join("\n", importantFiles);
//
//        String readmeSnippet = (readmeContent != null && readmeContent.length() > 2000)
//                ? readmeContent.substring(0, 2000)
//                : (readmeContent != null ? readmeContent : "No README");
//
//        // 2. Prompt
//        String promptText = String.format("""
//            You are a Senior Software Architect. Analyze this repo and return raw JSON.
//
//            CONTEXT:
//            - Type: %s
//            - Logic Score: %d/100
//
//            FILES (Truncated):
//            %s
//
//            README:
//            %s
//
//            INSTRUCTIONS:
//            1. Detect Tech Stack (Frontend, Backend, Infra).
//            2. Provide 5-7 Roadmap Improvements (title, description, category).
//            3. Write a concise Summary (Do NOT mention the numeric score %d).
//            4. Calculate a 'quality_bonus' (-10 to +10) based on best practices.
//
//            OUTPUT JSON ONLY:
//            {
//                "tech_stack": { "frontend": [], "backend": [], "infrastructure": [] },
//                "summary": "...",
//                "roadmap": [ { "title": "...", "description": "...", "category": "..." } ],
//                "quality_bonus": 0
//            }
//            """,
//                isMonorepo ? "Monorepo" : "Standard",
//                baseScore,
//                fileSummary,
//                readmeSnippet.replace("\"", "'"),
//                baseScore
//        );
//
//        // 3. Request
//        Map<String, Object> requestBody = Map.of(
//                "contents", List.of(Map.of("parts", List.of(Map.of("text", promptText))))
//        );
//
//        try {
//            JsonNode response = webClient.post()
//                    .uri(url)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(JsonNode.class)
//                    .block();
//
//            if (response != null && response.has("candidates")) {
//                String rawText = response.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
//                String cleanJson = rawText.replaceAll("```json", "").replaceAll("```", "").trim();
//
//                // Map JSON to DTO
//                AnalysisResult result = objectMapper.readValue(cleanJson, AnalysisResult.class);
//                return result;
//            }
//
//        } catch (WebClientResponseException e) {
//            System.err.println("❌ AI API Failed. Code: " + e.getStatusCode() + " Body: " + e.getResponseBodyAsString());
//
//            // IF 1.5-Flash fails again, fallback to 1.0-pro (Reliable Backup)
//            if (e.getStatusCode().value() == 404) {
//                System.err.println("⚠️ Model not found. Check if your API Key supports Flash, or try 'gemini-pro'");
//            }
//
//        } catch (Exception e) {
//            System.err.println("❌ AI Internal Error: " + e.getMessage());
//        }
//
//        return fallbackResult(baseScore);
//    }
//
//    private boolean isPriorityFile(String path) {
//        for (String key : PRIORITY_FILES) {
//            if (path.contains(key)) return true;
//        }
//        return false;
//    }
//
//    private long countOccurrences(String str, char ch) {
//        return str.chars().filter(c -> c == ch).count();
//    }
//
//    private AnalysisResult fallbackResult(int baseScore) {
//        AnalysisResult fallback = new AnalysisResult();
//        fallback.setScore(baseScore);
//        fallback.setSummary("AI Analysis Unavailable. Using calculated metrics.");
//        fallback.setRoadmap(List.of(
//                new AnalysisResult.RoadmapItem("Check System", "AI Service could not be reached.", "System")
//        ));
//        fallback.setTech_stack(new AnalysisResult.TechStack(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
//        return fallback;
//    }
//}


//package com.gitgrade.GitGradeSpring.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.gitgrade.GitGradeSpring.dto.AnalysisResult;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//public class AIService {
//
//    @Value("${app.gemini.key}")
//    private String geminiKey;
//
//    private final WebClient webClient = WebClient.create();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    // ✅ PORTED: Exact priority files list from your Python code
//    private static final Set<String> PRIORITY_FILES = Set.of(
//            "package.json", "pom.xml", "build.gradle", "requirements.txt",
//            "Dockerfile", "docker-compose.yml", "vite.config", "next.config",
//            "tsconfig.json", "go.mod", "Cargo.toml", "App.tsx", "main.py",
//            "tailwind.config.js", ".github/workflows"
//    );
//
//    public AnalysisResult analyzeCodeQuality(String readmeContent, List<String> files, int baseScore) {
//
//        // ✅ CORRECT URL for the "Flash/Lite" model (1500 req/day limit)
//        // Note: The API name for "Flash" is 'gemini-1.5-flash'
//        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiKey;
//
//        // --- 1. PYTHON LOGIC PORT: File Filtering ---
//        String allFilesStr = String.join(" ", files);
//        boolean isMonorepo = allFilesStr.contains("backend/") && allFilesStr.contains("frontend/");
//        boolean hasGitignore = allFilesStr.contains(".gitignore");
//
//        // Logic: Keep file if it's in PRIORITY_FILES OR depth <= 2
//        List<String> importantFiles = files.stream()
//                .filter(f -> isPriorityMatch(f) || countOccurrences(f, '/') <= 2)
//                .limit(80) // Python: important_files[:80]
//                .collect(Collectors.toList());
//
//        String fileSummary = String.join("\n", importantFiles);
//
//        // Truncate Readme (Python: readme_content[:1000])
//        String readmeSnippet = (readmeContent != null && readmeContent.length() > 1000)
//                ? readmeContent.substring(0, 1000)
//                : (readmeContent != null ? readmeContent : "No README detected.");
//
//        // --- 2. PYTHON LOGIC PORT: The "Harsh Architect" Prompt ---
//        String promptText = String.format("""
//            You are a harsh but helpful Senior Software Architect.
//            Analyze this GitHub repository structure and return a raw JSON response.
//
//            CONTEXT:
//            - Project Type: %s
//            - Base Logic Score: %d/100
//            - Has .gitignore: %s (Do NOT suggest adding it)
//
//            FILES DETECTED (Truncated):
//            %s
//
//            README EXCERPT:
//            %s
//
//            INSTRUCTIONS:
//            1. **Deep Tech Stack Detection:** Don't just say "JavaScript". Detect specific frameworks (e.g., "React", "Spring Boot", "Tailwind", "Vite").
//            2. **Detailed Roadmap:** Provide **5 to 7** distinct, high-impact improvements.
//            3. **Structure:** Each roadmap item MUST have a short 'title' and a detailed 'description' explaining HOW to do it.
//            4. **Summary:** Write a concise executive summary. **IMPORTANT: Do NOT mention the specific numeric 'Logic Score' (e.g. %d) in the text summary.** Just focus on the code quality description.
//
//            OUTPUT FORMAT (JSON ONLY - NO MARKDOWN):
//            {
//                "tech_stack": {
//                    "frontend": ["List", "frontend", "techs"],
//                    "backend": ["List", "backend", "techs"],
//                    "infrastructure": ["Docker", "AWS", "etc"]
//                },
//                "summary": "A 3-4 sentence executive summary of the architecture and code quality.",
//                "roadmap": [
//                    {
//                        "title": "Implement Docker Orchestration",
//                        "description": "Currently, the services run independently. Create a docker-compose.yml file...",
//                        "category": "Architecture"
//                    }
//                ],
//                "quality_bonus": 0
//            }
//            """,
//                isMonorepo ? "Full-Stack Monorepo" : "Standard Repository",
//                baseScore,
//                hasGitignore ? "YES" : "NO",
//                fileSummary,
//                readmeSnippet.replace("\"", "'").replace("\n", " "), // Sanitize for JSON string
//                baseScore
//        );
//
//        // 3. Prepare Request Body
//        Map<String, Object> requestBody = Map.of(
//                "contents", List.of(Map.of("parts", List.of(Map.of("text", promptText))))
//        );
//
//        // 4. Execute API Call
//        try {
//            JsonNode response = webClient.post()
//                    .uri(url)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(JsonNode.class)
//                    .block();
//
//            if (response != null && response.has("candidates")) {
//                String rawText = response.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
//                String cleanJson = rawText.replaceAll("```json", "").replaceAll("```", "").trim();
//
//                return objectMapper.readValue(cleanJson, AnalysisResult.class);
//            }
//
//        } catch (WebClientResponseException e) {
//            System.err.println("❌ AI API Failed. Code: " + e.getStatusCode());
//        } catch (Exception e) {
//            System.err.println("❌ AI Internal Error: " + e.getMessage());
//        }
//
//        return fallbackResult(baseScore);
//    }
//
//    // Helper: Check if file matches partial strings in priority list (like "vite.config")
//    private boolean isPriorityMatch(String path) {
//        for (String key : PRIORITY_FILES) {
//            if (path.contains(key)) return true;
//        }
//        return false;
//    }
//
//    private long countOccurrences(String str, char ch) {
//        return str.chars().filter(c -> c == ch).count();
//    }
//
//    private AnalysisResult fallbackResult(int baseScore) {
//        AnalysisResult fallback = new AnalysisResult();
//        fallback.setScore(baseScore);
//        fallback.setSummary("AI Analysis Unavailable. Using calculated metrics.");
//        fallback.setRoadmap(List.of(
//                new AnalysisResult.RoadmapItem("Check System", "AI Service could not be reached.", "System")
//        ));
//        fallback.setTech_stack(new AnalysisResult.TechStack(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
//        return fallback;
//    }
//}




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