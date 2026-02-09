


package com.gitgrade.GitGradeSpring.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class KeepAliveScheduler {

    // Inject your deployed Render URL from properties
    @Value("${app.deploy.url}")
    private String siteUrl;

    private final WebClient webClient = WebClient.create();


    @Scheduled(fixedRate = 440000)
    public void keepAlive() {
        if (siteUrl == null || siteUrl.contains("localhost")) {
            System.out.println("⚠ Keep-Alive skipped (Running locally or URL not set)");
            return;
        }

        try {
            System.out.println(" Pinging self to keep Render alive: " + siteUrl);
            webClient.get()
                    .uri(siteUrl) // Hits your own backend root URL
                    .retrieve()
                    .toBodilessEntity()
                    .block(); // Block is fine here since it's a background thread
            System.out.println(" Ping successful!");
        } catch (Exception e) {
            System.err.println("⚠ Keep-Alive Ping failed: " + e.getMessage());
        }
    }
}