package com.placehirex.placementbackend.controller;

import com.placehirex.placementbackend.dto.AdminAnalyticsResponse;
import com.placehirex.placementbackend.repository.StudentProfileRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {
    private static final String READY_LABEL = "Likely Placed";

    private final StudentProfileRepository studentProfileRepository;

    public AdminAnalyticsController(StudentProfileRepository studentProfileRepository) {
        this.studentProfileRepository = studentProfileRepository;
    }

    @GetMapping("/analytics")
    public AdminAnalyticsResponse getAnalytics() {
        long totalStudents = studentProfileRepository.count();
        long readyStudentsCount = studentProfileRepository.countByReadinessLabel(READY_LABEL);
        long notReadyStudentsCount = totalStudents - readyStudentsCount;
        Double averageScore = studentProfileRepository.findAverageReadinessScore();
        Double averageCGPA = studentProfileRepository.findAverageCGPA();

        long internshipReadyCount = studentProfileRepository.countByInternshipAndReadinessLabel(true, READY_LABEL);
        long nonInternshipReadyCount = studentProfileRepository.countByInternshipAndReadinessLabel(false, READY_LABEL);

        java.util.Map<String, Long> readinessDistribution = new java.util.LinkedHashMap<>();
        readinessDistribution.put("0-20", studentProfileRepository.countByReadinessScoreGreaterThanEqualAndReadinessScoreLessThan(0, 20));
        readinessDistribution.put("20-40", studentProfileRepository.countByReadinessScoreGreaterThanEqualAndReadinessScoreLessThan(20, 40));
        readinessDistribution.put("40-60", studentProfileRepository.countByReadinessScoreGreaterThanEqualAndReadinessScoreLessThan(40, 60));
        readinessDistribution.put("60-80", studentProfileRepository.countByReadinessScoreGreaterThanEqualAndReadinessScoreLessThan(60, 80));
        readinessDistribution.put("80-100", studentProfileRepository.countByReadinessScoreGreaterThanEqualAndReadinessScoreLessThanEqual(80, 100));

        return new AdminAnalyticsResponse(
                totalStudents,
                readyStudentsCount,
                notReadyStudentsCount,
                averageScore == null ? 0.0 : averageScore,
                averageCGPA == null ? 0.0 : averageCGPA,
                internshipReadyCount,
                nonInternshipReadyCount,
                readinessDistribution
        );
    }
}
