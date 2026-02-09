package com.gitgrade.GitGradeSpring;

import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ModelChecker {
    public static void main(String[] args) {
        // Load API Key
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String apiKey = dotenv.get("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ ERROR: GEMINI_API_KEY not found in .env file");
            return;
        }

        System.out.println("🔍 Checking available models for your API Key...");

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ SUCCESS! Here are your available models:\n");
                System.out.println(response.body());
            } else {
                System.err.println("❌ FAILED. Status Code: " + response.statusCode());
                System.err.println("Response: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}