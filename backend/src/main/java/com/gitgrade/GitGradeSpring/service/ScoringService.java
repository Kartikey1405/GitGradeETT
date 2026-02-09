package com.gitgrade.GitGradeSpring.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ScoringService {

    public int calculateScore(JsonNode metadata, List<String> files, String readmeContent) {
        int score = 0;
        String allFilesStr = String.join(" ", files);
        String readmeLower = (readmeContent != null) ? readmeContent.toLowerCase() : "";

        // --- Edge Case: Empty Repo ---
        if (files.size() < 2) return 10;

        // =========================================================
        // 1. REPOSITORY HYGIENE (Max 20 pts)
        // =========================================================

        // A. Root Directory Clutter
        // Count files that do NOT have a slash (meaning they are in root)
        long rootFileCount = files.stream().filter(f -> !f.contains("/")).count();
        if (rootFileCount < 15) score += 5;

        // B. The "Trash" Check (Negative Scoring)
        String[] garbageFiles = {".DS_Store", "Thumbs.db", "__pycache__", "node_modules", ".env", "venv"};
        int penalty = 0;
        for (String junk : garbageFiles) {
            if (allFilesStr.contains(junk)) penalty += 5;
        }
        score = Math.max(0, score - penalty);

        // C. Standard Folders
        String[] structureFolders = {"src/", "app/", "lib/", "components/", "pages/", "public/", "server/", "client/"};
        boolean hasStructure = false;
        for (String folder : structureFolders) {
            if (allFilesStr.contains(folder)) {
                hasStructure = true;
                break;
            }
        }
        if (hasStructure) score += 15;
        else if (files.size() > 3) score += 5;

        // =========================================================
        // 2. DOCUMENTATION DEPTH (Max 20 pts)
        // =========================================================
        if (readmeContent != null && !readmeContent.isEmpty()) {
            // A. Length
            if (readmeContent.length() > 1000) score += 5;
            else if (readmeContent.length() > 300) score += 2;

            // B. Setup Instructions
            String[] setupKeywords = {"npm install", "pip install", "setup", "getting started", "run the app", "docker run", "mvn install"};
            for (String k : setupKeywords) {
                if (readmeLower.contains(k)) {
                    score += 10;
                    break;
                }
            }

            // C. Visuals/Badges
            if (readmeContent.contains("![") || readmeLower.contains("<img")) score += 5;
        }

        // =========================================================
        // 3. ENGINEERING STANDARDS (Max 20 pts)
        // =========================================================

        // A. Version Control
        if (files.contains(".gitignore") || allFilesStr.contains(".gitignore")) score += 10;

        // B. Dependency Locking
        String[] lockFiles = {"package-lock.json", "yarn.lock", "pom.xml", "build.gradle", "requirements.txt", "go.mod"};
        for (String f : lockFiles) {
            if (allFilesStr.contains(f)) {
                score += 10;
                break;
            }
        }

        // =========================================================
        // 4. TESTING & QA (Max 20 pts)
        // =========================================================
        String[] testIndicators = {"test", "tests", "__tests__", "spec", "pytest.ini"};
        for (String t : testIndicators) {
            if (allFilesStr.contains(t)) {
                score += 15;
                break;
            }
        }

        // CI/CD Check
        if (allFilesStr.contains(".github") || allFilesStr.contains(".travis.yml") || allFilesStr.contains("circleci")) {
            score += 5;
        }

        // =========================================================
        // 5. ACTIVITY & HEALTH (Max 10 pts)
        // =========================================================
        if (metadata.has("pushed_at")) {
            try {
                String pushedDate = metadata.get("pushed_at").asText(); // ISO 8601 format
                Instant lastPush = Instant.parse(pushedDate);
                long daysInactive = ChronoUnit.DAYS.between(lastPush, Instant.now());

                if (daysInactive < 30) score += 10;
                else if (daysInactive < 90) score += 5;
            } catch (Exception e) {
                score += 5; // Benefit of doubt if date parsing fails
            }
        }

        // =========================================================
        // 6. GOLDEN STANDARD (Max 10 pts)
        // =========================================================
        if (metadata.has("license") && !metadata.get("license").isNull()) score += 5;
        if (metadata.has("description") && metadata.get("description").asText().length() > 10) score += 5;

        // Ensure score stays between 0 and 100
        return Math.min(100, Math.max(0, score));
    }
}
