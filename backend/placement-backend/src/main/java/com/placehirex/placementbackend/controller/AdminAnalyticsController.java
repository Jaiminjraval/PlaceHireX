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

        return new AdminAnalyticsResponse(
                totalStudents,
                readyStudentsCount,
                notReadyStudentsCount,
                averageScore == null ? 0.0 : averageScore
        );
    }
}
