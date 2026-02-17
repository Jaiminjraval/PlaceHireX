package com.placehirex.placementbackend.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionHistoryResponse {
    private double predictionScore;
    private String predictionLabel;
    private LocalDateTime timestamp;
}
