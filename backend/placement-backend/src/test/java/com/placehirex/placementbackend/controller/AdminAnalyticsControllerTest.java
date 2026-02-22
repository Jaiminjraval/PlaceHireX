package com.placehirex.placementbackend.controller;

import com.placehirex.placementbackend.repository.StudentProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.placehirex.placementbackend.security.JwtAuthenticationFilter;
import com.placehirex.placementbackend.security.RestAccessDeniedHandler;
import com.placehirex.placementbackend.security.RestAuthenticationEntryPoint;
import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(AdminAnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass security filters
class AdminAnalyticsControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private StudentProfileRepository studentProfileRepository;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private UserDetailsService userDetailsService;

        @MockBean
        private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

        @MockBean
        private RestAccessDeniedHandler restAccessDeniedHandler;

        @Test
        void getAnalytics_ShouldReturnEnhancedAnalytics() throws Exception {
                // Given
                given(studentProfileRepository.count()).willReturn(100L);
                given(studentProfileRepository.countByReadinessLabel("Likely Placed")).willReturn(60L);
                given(studentProfileRepository.findAverageReadinessScore()).willReturn(75.5);
                given(studentProfileRepository.findAverageCGPA()).willReturn(8.2);

                // Internship stats
                given(studentProfileRepository.countByInternshipAndReadinessLabel(true, "Likely Placed"))
                                .willReturn(40L);
                given(studentProfileRepository.countByInternshipAndReadinessLabel(false, "Likely Placed"))
                                .willReturn(20L);

                // Histograms
                given(studentProfileRepository.countByReadinessScoreGreaterThanEqualAndReadinessScoreLessThan(0, 20))
                                .willReturn(5L);
                given(studentProfileRepository.countByReadinessScoreGreaterThanEqualAndReadinessScoreLessThan(20, 40))
                                .willReturn(10L);
                given(studentProfileRepository.countByReadinessScoreGreaterThanEqualAndReadinessScoreLessThan(40, 60))
                                .willReturn(15L);
                given(studentProfileRepository.countByReadinessScoreGreaterThanEqualAndReadinessScoreLessThan(60, 80))
                                .willReturn(50L);
                given(studentProfileRepository.countByReadinessScoreGreaterThanEqualAndReadinessScoreLessThanEqual(80,
                                100))
                                .willReturn(20L);

                // When/Then
                mockMvc.perform(get("/api/admin/analytics")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalStudents").value(100))
                                .andExpect(jsonPath("$.readyStudentsCount").value(60))
                                .andExpect(jsonPath("$.notReadyStudentsCount").value(40)) // 100 - 60
                                .andExpect(jsonPath("$.averageReadinessScore").value(75.5))
                                .andExpect(jsonPath("$.averageCGPA").value(8.2))
                                .andExpect(jsonPath("$.internshipReadyCount").value(40))
                                .andExpect(jsonPath("$.nonInternshipReadyCount").value(20))
                                .andExpect(jsonPath("$.readinessDistribution['0-20']").value(5))
                                .andExpect(jsonPath("$.readinessDistribution['20-40']").value(10))
                                .andExpect(jsonPath("$.readinessDistribution['40-60']").value(15))
                                .andExpect(jsonPath("$.readinessDistribution['60-80']").value(50))
                                .andExpect(jsonPath("$.readinessDistribution['80-100']").value(20));
        }
}
