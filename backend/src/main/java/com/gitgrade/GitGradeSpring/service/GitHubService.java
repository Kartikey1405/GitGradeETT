package com.gitgrade.GitGradeSpring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gitgrade.GitGradeSpring.dto.RepoData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class GitHubService {

    @Value("${app.github.token}")
    private String githubToken;

    private final WebClient webClient;

    public GitHubService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.github.com").build();
    }

    public RepoData fetchRepoData(String owner, String repoName) {
        // 1. Fetch Metadata
        JsonNode metadata = webClient.get()
                .uri("/repos/{owner}/{repo}", owner, repoName)
                .header("Authorization", "token " + githubToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(); // Synchronous blocking for simplicity

        // 2. Fetch File Tree (Recursive)
        // Default to 'main' branch if not specified
        String defaultBranch = metadata.has("default_branch") ? metadata.get("default_branch").asText() : "main";
        List<String> files = new ArrayList<>();

        try {
            JsonNode treeResponse = webClient.get()
                    .uri("/repos/{owner}/{repo}/git/trees/{branch}?recursive=1", owner, repoName, defaultBranch)
                    .header("Authorization", "token " + githubToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (treeResponse != null && treeResponse.has("tree")) {
                for (JsonNode item : treeResponse.get("tree")) {
                    if ("blob".equals(item.get("type").asText())) {
                        files.add(item.get("path").asText());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not fetch file tree: " + e.getMessage());
        }

        // 3. Fetch Readme
        String readmeContent = "";
        try {
            JsonNode readmeJson = webClient.get()
                    .uri("/repos/{owner}/{repo}/readme", owner, repoName)
                    .header("Authorization", "token " + githubToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (readmeJson != null && readmeJson.has("content")) {
                // GitHub sends Base64 with newlines, we must remove them before decoding
                String encoded = readmeJson.get("content").asText().replace("\n", "");
                readmeContent = new String(Base64.getDecoder().decode(encoded));
            }
        } catch (Exception e) {
            System.out.println("Warning: No README found.");
        }

        // Return the DTO we created earlier
        return new RepoData(metadata, files, readmeContent);
    }
}