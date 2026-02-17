package com.placehirex.placementbackend.service;

import com.placehirex.placementbackend.dto.PredictionRequest;
import com.placehirex.placementbackend.dto.PredictionResponse;
import com.placehirex.placementbackend.exception.PredictionServiceException;
import com.placehirex.placementbackend.model.PredictionHistory;
import com.placehirex.placementbackend.model.StudentProfile;
import com.placehirex.placementbackend.repository.PredictionHistoryRepository;
import com.placehirex.placementbackend.repository.StudentProfileRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@Service
public class PredictionService {
    private final WebClient webClient;
    private final StudentProfileRepository studentProfileRepository;
    private final PredictionHistoryRepository predictionHistoryRepository;

    @Autowired
    public PredictionService(
            WebClient.Builder webClientBuilder,
            @Value("${ml.api.base-url:http://localhost:8000}") String mlApiBaseUrl,
            StudentProfileRepository studentProfileRepository,
            PredictionHistoryRepository predictionHistoryRepository
    ) {
        this.webClient = webClientBuilder.baseUrl(mlApiBaseUrl).build();
        this.studentProfileRepository = studentProfileRepository;
        this.predictionHistoryRepository = predictionHistoryRepository;
    }

    PredictionService(WebClient.Builder webClientBuilder, String mlApiBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(mlApiBaseUrl).build();
        this.studentProfileRepository = null;
        this.predictionHistoryRepository = null;
    }

    public PredictionResponse getPrediction(PredictionRequest request) {
        try {
            PredictionResponse response = webClient.post()
                    .uri("/predict")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("No error details")
                                    .map(body -> new PredictionServiceException(
                                            "Prediction API returned error: " + body,
                                            HttpStatus.BAD_GATEWAY
                                    ))
                    )
                    .bodyToMono(PredictionResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response == null) {
                throw new PredictionServiceException(
                        "Prediction API returned an empty response",
                        HttpStatus.BAD_GATEWAY
                );
            }

            return response;
        } catch (PredictionServiceException ex) {
            throw ex;
        } catch (WebClientRequestException ex) {
            throw new PredictionServiceException(
                    "Prediction API is unavailable. Please try again later.",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ex
            );
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof java.util.concurrent.TimeoutException) {
                throw new PredictionServiceException(
                        "Prediction API is unavailable. Please try again later.",
                        HttpStatus.SERVICE_UNAVAILABLE,
                        ex
                );
            }
            throw new PredictionServiceException(
                    "Failed to process prediction response",
                    HttpStatus.BAD_GATEWAY,
                    ex
            );
        } catch (Exception ex) {
            throw new PredictionServiceException(
                    "Failed to process prediction response",
                    HttpStatus.BAD_GATEWAY,
                    ex
            );
        }
    }

    @Transactional
    public PredictionResponse predictAndPersist(StudentProfile studentProfile) {
        Objects.requireNonNull(studentProfileRepository, "StudentProfileRepository is not configured");
        Objects.requireNonNull(predictionHistoryRepository, "PredictionHistoryRepository is not configured");

        PredictionRequest request = new PredictionRequest();
        request.setCgpa(studentProfile.getCgpa());
        request.setDsaRating(studentProfile.getDsaRating());
        request.setProjectsCount(studentProfile.getProjectsCount());
        request.setInternship(studentProfile.isInternship());
        request.setAttendance(studentProfile.getAttendance());
        request.setAptitudeScore(studentProfile.getAptitudeScore());

        PredictionResponse response = getPrediction(request);

        studentProfile.setReadinessScore(response.getProbability());
        studentProfile.setReadinessLabel(response.getLabel());
        StudentProfile savedProfile = studentProfileRepository.save(studentProfile);

        PredictionHistory history = new PredictionHistory();
        history.setStudentProfile(savedProfile);
        history.setPredictionScore(savedProfile.getReadinessScore());
        history.setPredictionLabel(savedProfile.getReadinessLabel());
        predictionHistoryRepository.save(history);

        return new PredictionResponse(
                savedProfile.getReadinessScore(),
                savedProfile.getReadinessLabel(),
                buildExplanations(savedProfile),
                buildRecommendations(savedProfile)
        );
    }

    private List<String> buildExplanations(StudentProfile profile) {
        List<String> explanations = new ArrayList<>();
        if (profile.getAptitudeScore() < 50) {
            explanations.add("Low aptitude score");
        }
        if (profile.getAttendance() < 75) {
            explanations.add("Low attendance");
        }
        if (profile.getProjectsCount() < 2) {
            explanations.add("Few projects");
        }
        if (!profile.isInternship()) {
            explanations.add("No internship experience");
        }
        return explanations;
    }

    private List<String> buildRecommendations(StudentProfile profile) {
        List<String> recommendations = new ArrayList<>();
        if (profile.getDsaRating() < 3) {
            recommendations.add("Increase DSA practice to at least level 3");
        }
        if (profile.getProjectsCount() < 2) {
            recommendations.add("Build two or more quality projects");
        }
        if (!profile.isInternship()) {
            recommendations.add("Try to secure at least one internship");
        }
        if (profile.getAttendance() < 80) {
            recommendations.add("Improve attendance above 80%");
        }
        return recommendations;
    }
}
