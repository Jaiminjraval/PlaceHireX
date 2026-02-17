package com.placehirex.placementbackend.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PredictionResponse {
    private double probability;
    private String label;
    private List<String> explanations = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();

    public PredictionResponse(double probability, String label) {
        this.probability = probability;
        this.label = label;
        this.explanations = new ArrayList<>();
        this.recommendations = new ArrayList<>();
    }

    public PredictionResponse(double probability, String label, List<String> explanations) {
        this.probability = probability;
        this.label = label;
        this.explanations = explanations == null ? new ArrayList<>() : explanations;
        this.recommendations = new ArrayList<>();
    }

    public PredictionResponse(
            double probability,
            String label,
            List<String> explanations,
            List<String> recommendations
    ) {
        this.probability = probability;
        this.label = label;
        this.explanations = explanations == null ? new ArrayList<>() : explanations;
        this.recommendations = recommendations == null ? new ArrayList<>() : recommendations;
    }
}
