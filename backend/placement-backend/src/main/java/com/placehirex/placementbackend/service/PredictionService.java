package com.placehirex.placementbackend.service;

import com.placehirex.placementbackend.dto.PredictionRequest;
import com.placehirex.placementbackend.dto.PredictionResponse;
import com.placehirex.placementbackend.exception.PredictionServiceException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@Service
public class PredictionService {
    private final WebClient webClient;

    public PredictionService(
            WebClient.Builder webClientBuilder,
            @Value("${ml.api.base-url:http://localhost:8000}") String mlApiBaseUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(mlApiBaseUrl).build();
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
}
