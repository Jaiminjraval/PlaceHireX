package com.placehirex.placementbackend.dto;

import lombok.Data;

@Data
public class PredictionRequest {
    private double cgpa;
    private int dsa_rating;
    private int projects;
    private int internship;
    private int attendance;
    private int aptitude_score;
}
