package com.gitgrade.GitGradeSpring.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "analyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship: Many Analyses -> One User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "repo_name")
    private String repoName;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(columnDefinition = "TEXT") // Allows storing long summaries
    private String summary;

    @Column(name = "full_json_result", columnDefinition = "TEXT") // We store the raw JSON string here
    private String fullJsonResult;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
