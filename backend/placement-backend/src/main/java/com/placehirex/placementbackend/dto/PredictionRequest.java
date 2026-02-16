package com.placehirex.placementbackend.dto;

import lombok.Data;

@Data
public class PredictionRequest {
    private double cgpa;
    private int dsaRating;
    private int projectsCount;
    private boolean internship;
    private double attendance;
    private double aptitudeScore;
}
