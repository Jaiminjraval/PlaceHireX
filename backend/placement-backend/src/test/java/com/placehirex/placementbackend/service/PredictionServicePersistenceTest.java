package com.placehirex.placementbackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.placehirex.placementbackend.dto.PredictionResponse;
import com.placehirex.placementbackend.model.AppUser;
import com.placehirex.placementbackend.model.PredictionHistory;
import com.placehirex.placementbackend.model.Role;
import com.placehirex.placementbackend.model.StudentProfile;
import com.placehirex.placementbackend.repository.PredictionHistoryRepository;
import com.placehirex.placementbackend.repository.StudentProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class PredictionServicePersistenceTest {
    @Mock
    private StudentProfileRepository studentProfileRepository;
    @Mock
    private PredictionHistoryRepository predictionHistoryRepository;
    @Mock
    private com.placehirex.placementbackend.repository.ModelVersionRepository modelVersionRepository;

    @Test
    void predictAndPersistShouldUpdateProfileAndSaveHistory() {
        PredictionService service = new PredictionService(
                WebClient.builder(),
                "http://localhost:8000",
                studentProfileRepository,
                predictionHistoryRepository,
                modelVersionRepository);
        PredictionService serviceSpy = org.mockito.Mockito.spy(service);

        AppUser appUser = new AppUser();
        appUser.setId(1L);
        appUser.setEmail("student@example.com");
        appUser.setPassword("x");
        appUser.setRole(Role.STUDENT);
        appUser.setEnabled(true);

        StudentProfile profile = new StudentProfile();
        profile.setId(10L);
        profile.setUser(appUser);
        profile.setCgpa(7.9);
        profile.setDsaRating(2);
        profile.setProjectsCount(1);
        profile.setInternship(false);
        profile.setAttendance(70);
        profile.setAptitudeScore(45);

        doReturn(new PredictionResponse(0.72, "Ready")).when(serviceSpy).getPrediction(any());
        when(studentProfileRepository.save(any(StudentProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(predictionHistoryRepository.save(any(PredictionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PredictionResponse response = serviceSpy.predictAndPersist(profile);

        assertEquals(0.72, response.getProbability(), 0.0001);
        assertEquals("Ready", response.getLabel());
        assertEquals(4, response.getExplanations().size());
        assertEquals(4, response.getRecommendations().size());
        assertEquals("Increase DSA practice to at least level 3", response.getRecommendations().get(0));
        assertEquals("Build two or more quality projects", response.getRecommendations().get(1));
        assertEquals("Try to secure at least one internship", response.getRecommendations().get(2));
        assertEquals("Improve attendance above 80%", response.getRecommendations().get(3));
        verify(studentProfileRepository, times(1)).save(any(StudentProfile.class));
        verify(predictionHistoryRepository, times(1)).save(any(PredictionHistory.class));

        ArgumentCaptor<PredictionHistory> historyCaptor = ArgumentCaptor.forClass(PredictionHistory.class);
        verify(predictionHistoryRepository).save(historyCaptor.capture());
        assertEquals(0.72, historyCaptor.getValue().getPredictionScore(), 0.0001);
        assertEquals("Ready", historyCaptor.getValue().getPredictionLabel());
        assertNotNull(historyCaptor.getValue().getStudentProfile());
    }
}
