package com.gitgrade.GitGradeSpring.service;

import com.gitgrade.GitGradeSpring.dto.AnalysisResult;
import com.gitgrade.GitGradeSpring.dto.AnalysisResult.RoadmapItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;
import java.util.List;

@Service
public class EmailService {

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.apikey}")
    private String sendGridApiKey;

    private final WebClient webClient = WebClient.create();

    public String generatePdf(AnalysisResult data) throws Exception {
        // 1. Setup File
        String fileName = "reports/" + data.getDetails().getName() + "_report.pdf";
        File directory = new File("reports");
        if (!directory.exists()) directory.mkdirs();

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(fileName));

        document.open();

        // 2. Add Content (Replacing FPDF Logic)
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        // Title
        Paragraph title = new Paragraph("GitGrade Analysis Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        // Repo Details
        document.add(new Paragraph("Repository: " + data.getDetails().getOwner() + "/" + data.getDetails().getName(), headerFont));
        document.add(new Paragraph("Language: " + data.getDetails().getLanguage(), normalFont));
        document.add(new Paragraph("Stars: " + data.getDetails().getStars() + " | Forks: " + data.getDetails().getForks(), normalFont));
        document.add(new Paragraph("\n"));

        // Score
        document.add(new Paragraph("Final Grade: " + data.getScore() + "/100", titleFont));
        document.add(new Paragraph("\n"));

        // Summary
        document.add(new Paragraph("Executive Summary:", headerFont));
        document.add(new Paragraph(data.getSummary(), normalFont));
        document.add(new Paragraph("\n"));

        // Roadmap
        document.add(new Paragraph("Improvement Roadmap:", headerFont));
        if (data.getRoadmap() != null) {
            int i = 1;
            for (RoadmapItem item : data.getRoadmap()) {
                Paragraph p = new Paragraph(i + ". " + item.getTitle() + " [" + item.getCategory() + "]", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11));
                document.add(p);
                document.add(new Paragraph("   " + item.getDescription(), normalFont));
                document.add(new Paragraph("\n"));
                i++;
            }
        }

        document.close();
        return fileName;
    }

    public boolean sendEmail(String toEmail, String pdfPath) {
        try {
            // 1. Read PDF and Convert to Base64
            File file = new File(pdfPath);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String encodedPdf = Base64.getEncoder().encodeToString(fileContent);

            // 2. Construct SendGrid JSON Payload (Raw Map to avoid extra DTOs)
            Map<String, Object> payload = Map.of(
                    "personalizations", List.of(Map.of("to", List.of(Map.of("email", toEmail)))),
                    "from", Map.of("email", fromEmail),
                    "subject", "Your GitGrade Analysis Report 🚀",
                    "content", List.of(Map.of("type", "text/html", "value", "<strong>Here is your detailed GitHub analysis report attached below.</strong><br><br>Keep coding!<br>- The GitGrade Team")),
                    "attachments", List.of(Map.of(
                            "content", encodedPdf,
                            "filename", file.getName(),
                            "type", "application/pdf",
                            "disposition", "attachment"
                    ))
            );

            // 3. Send via WebClient
            webClient.post()
                    .uri("https://api.sendgrid.com/v3/mail/send")
                    .header("Authorization", "Bearer " + sendGridApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return true;

        } catch (Exception e) {
            System.err.println("SendGrid Error: " + e.getMessage());
            return false;
        }
    }
}