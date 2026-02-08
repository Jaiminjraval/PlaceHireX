package com.placehirex.placementbackend.service;

import com.placehirex.placementbackend.dto.PredictionRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PredictionService {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String ML_API_URL = "http://localhost:5000/predict";

    public Map<String, Object> getPrediction(PredictionRequest request) {
        return restTemplate.postForObject(
                ML_API_URL,
                request,
                Map.class
        );
    }
}
