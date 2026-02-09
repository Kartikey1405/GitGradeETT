package com.gitgrade.GitGradeSpring.repository;

import com.gitgrade.GitGradeSpring.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    // Find all reports belonging to a specific user
    List<Analysis> findByOwnerId(Long userId);
}