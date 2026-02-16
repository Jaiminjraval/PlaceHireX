package com.placehirex.placementbackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.placehirex.placementbackend.dto.PredictionRequest;
import com.placehirex.placementbackend.dto.PredictionResponse;
import com.placehirex.placementbackend.exception.PredictionServiceException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

class PredictionServiceTest {
    private MockWebServer mockWebServer;
    private PredictionService predictionService;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        predictionService = new PredictionService(
                WebClient.builder(),
                mockWebServer.url("/").toString()
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnPredictionResponseOnSuccess() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"probability\":0.91,\"label\":\"Likely Placed\"}")
                .addHeader("Content-Type", "application/json"));

        PredictionResponse response = predictionService.getPrediction(sampleRequest());

        assertEquals(0.91, response.getProbability(), 0.0001);
        assertEquals("Likely Placed", response.getLabel());
    }

    @Test
    void shouldThrowPredictionServiceExceptionOnHttpError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\":\"model crashed\"}")
                .addHeader("Content-Type", "application/json"));

        PredictionServiceException ex = assertThrows(
                PredictionServiceException.class,
                () -> predictionService.getPrediction(sampleRequest())
        );

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatus());
    }

    @Test
    void shouldThrowPredictionServiceExceptionOnConnectionFailure() throws Exception {
        mockWebServer.shutdown();

        PredictionServiceException ex = assertThrows(
                PredictionServiceException.class,
                () -> predictionService.getPrediction(sampleRequest())
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatus());
    }

    private PredictionRequest sampleRequest() {
        PredictionRequest request = new PredictionRequest();
        request.setCgpa(8.2);
        request.setDsaRating(7);
        request.setProjectsCount(3);
        request.setInternship(true);
        request.setAttendance(90.0);
        request.setAptitudeScore(85.0);
        return request;
    }
}
