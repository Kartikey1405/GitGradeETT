package com.gitgrade.GitGradeSpring.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepoData {
    // This holds the data we fetch from GitHub before processing it
    private JsonNode metadata;
    private List<String> files;
    private String readmeContent;
}