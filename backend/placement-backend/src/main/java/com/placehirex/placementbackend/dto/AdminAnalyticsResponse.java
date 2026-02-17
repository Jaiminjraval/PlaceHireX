package com.placehirex.placementbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsResponse {
    private long totalStudents;
    private long readyStudentsCount;
    private long notReadyStudentsCount;
    private double averageReadinessScore;
}
