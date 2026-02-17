package com.placehirex.placementbackend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placehirex.placementbackend.dto.PredictionResponse;
import com.placehirex.placementbackend.model.AppUser;
import com.placehirex.placementbackend.model.PredictionHistory;
import com.placehirex.placementbackend.model.Role;
import com.placehirex.placementbackend.model.StudentProfile;
import com.placehirex.placementbackend.repository.AppUserRepository;
import com.placehirex.placementbackend.repository.PredictionHistoryRepository;
import com.placehirex.placementbackend.repository.StudentProfileRepository;
import com.placehirex.placementbackend.service.PredictionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private StudentProfileRepository studentProfileRepository;
    @Autowired
    private PredictionHistoryRepository predictionHistoryRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private PredictionService predictionService;

    @BeforeEach
    void setUp() {
        predictionHistoryRepository.deleteAll();
        studentProfileRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void registerAndLoginShouldBePublic() throws Exception {
        String registerBody = """
                {
                  "email":"student1@example.com",
                  "password":"password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("STUDENT"));

        String loginBody = """
                {
                  "email":"student1@example.com",
                  "password":"password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void helloShouldRequireToken() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void helloShouldReturnStudentContentForStudentRole() throws Exception {
        String token = registerAndGetToken("student2@example.com", "password123");

        mockMvc.perform(get("/api/hello")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.email").value("student2@example.com"));
    }

    @Test
    void helloShouldReturnAdminContentForAdminRole() throws Exception {
        AppUser admin = new AppUser();
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        appUserRepository.save(admin);

        String loginBody = """
                {
                  "email":"admin@example.com",
                  "password":"admin123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("token").asText();

        mockMvc.perform(get("/api/hello")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("admin@example.com"));
    }

    @Test
    void studentProfileEndpointsShouldCreateAndReadProfileForStudentOnly() throws Exception {
        String studentToken = registerAndGetToken("student3@example.com", "password123");

        AppUser admin = new AppUser();
        admin.setEmail("admin2@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        appUserRepository.save(admin);
        String adminToken = loginAndGetToken("admin2@example.com", "admin123");

        String payload = """
                {
                  "cgpa":8.2,
                  "dsaRating":7,
                  "projectsCount":3,
                  "internship":true,
                  "attendance":90.0,
                  "aptitudeScore":85.0
                }
                """;

        mockMvc.perform(post("/api/students/profile")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("student3@example.com"))
                .andExpect(jsonPath("$.cgpa").value(8.2))
                .andExpect(jsonPath("$.dsaRating").value(7))
                .andExpect(jsonPath("$.projectsCount").value(3));

        mockMvc.perform(get("/api/students/profile")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("student3@example.com"))
                .andExpect(jsonPath("$.cgpa").value(8.2));

        mockMvc.perform(post("/api/students/profile")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/students/profile")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentsPredictShouldUseExistingProfileAndAllowOnlyStudent() throws Exception {
        doAnswer(invocation -> {
            StudentProfile profile = invocation.getArgument(0);
            profile.setReadinessScore(0.78);
            profile.setReadinessLabel("Likely Placed");
            StudentProfile savedProfile = studentProfileRepository.save(profile);

            PredictionHistory history = new PredictionHistory();
            history.setStudentProfile(savedProfile);
            history.setPredictionScore(savedProfile.getReadinessScore());
            history.setPredictionLabel(savedProfile.getReadinessLabel());
            predictionHistoryRepository.save(history);

            return new PredictionResponse(0.78, "Likely Placed", List.of(
                    "Low aptitude score",
                    "Low attendance",
                    "Few projects",
                    "No internship experience"
            ), List.of(
                    "Increase DSA practice to at least level 3",
                    "Build two or more quality projects",
                    "Try to secure at least one internship",
                    "Improve attendance above 80%"
            ));
        }).when(predictionService).predictAndPersist(any());

        String studentToken = registerAndGetToken("student4@example.com", "password123");

        AppUser admin = new AppUser();
        admin.setEmail("admin4@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        appUserRepository.save(admin);
        String adminToken = loginAndGetToken("admin4@example.com", "admin123");

        String profilePayload = """
                {
                  "cgpa":8.2,
                  "dsaRating":7,
                  "projectsCount":1,
                  "internship":false,
                  "attendance":70.0,
                  "aptitudeScore":45.0
                }
                """;

        mockMvc.perform(post("/api/students/profile")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profilePayload))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/students/predict")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.probability").value(0.78))
                .andExpect(jsonPath("$.label").value("Likely Placed"))
                .andExpect(jsonPath("$.explanations", hasSize(4)))
                .andExpect(jsonPath("$.explanations[0]").value("Low aptitude score"))
                .andExpect(jsonPath("$.explanations[1]").value("Low attendance"))
                .andExpect(jsonPath("$.explanations[2]").value("Few projects"))
                .andExpect(jsonPath("$.explanations[3]").value("No internship experience"))
                .andExpect(jsonPath("$.recommendations", hasSize(4)))
                .andExpect(jsonPath("$.recommendations[0]").value("Increase DSA practice to at least level 3"))
                .andExpect(jsonPath("$.recommendations[1]").value("Build two or more quality projects"))
                .andExpect(jsonPath("$.recommendations[2]").value("Try to secure at least one internship"))
                .andExpect(jsonPath("$.recommendations[3]").value("Improve attendance above 80%"));

        mockMvc.perform(post("/api/students/predict")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/students/predict")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        StudentProfile studentProfile = studentProfileRepository.findFirstByUserEmail("student4@example.com")
                .orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(0.78, studentProfile.getReadinessScore(), 0.0001);
        org.junit.jupiter.api.Assertions.assertEquals("Likely Placed", studentProfile.getReadinessLabel());
        org.junit.jupiter.api.Assertions.assertEquals(1, predictionHistoryRepository.count());
    }

    @Test
    void studentsPredictShouldFailWhenProfileMissing() throws Exception {
        String studentToken = registerAndGetToken("student5@example.com", "password123");

        mockMvc.perform(post("/api/students/predict")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentsHistoryShouldReturnLast10DescendingForLoggedInStudent() throws Exception {
        String studentToken = registerAndGetToken("history-student@example.com", "password123");
        AppUser student = appUserRepository.findByEmail("history-student@example.com").orElseThrow();

        StudentProfile profile = new StudentProfile();
        profile.setUser(student);
        profile.setCgpa(8.0);
        profile.setDsaRating(4);
        profile.setProjectsCount(2);
        profile.setInternship(true);
        profile.setAttendance(85);
        profile.setAptitudeScore(80);
        profile.setReadinessScore(0.0);
        profile.setReadinessLabel("Needs Improvement");
        StudentProfile savedProfile = studentProfileRepository.save(profile);

        for (int i = 0; i < 12; i++) {
            PredictionHistory history = new PredictionHistory();
            history.setStudentProfile(savedProfile);
            history.setPredictionScore(0.1 * i);
            history.setPredictionLabel(i % 2 == 0 ? "Ready" : "Needs Improvement");
            history.setTimestamp(LocalDateTime.now().minusMinutes(i));
            predictionHistoryRepository.save(history);
        }

        mockMvc.perform(get("/api/students/history")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].predictionScore").value(0.0))
                .andExpect(jsonPath("$[9].predictionScore").value(0.9));
    }

    @Test
    void studentsHistoryShouldRejectAdminAndInvalidToken() throws Exception {
        AppUser admin = new AppUser();
        admin.setEmail("history-admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        appUserRepository.save(admin);
        String adminToken = loginAndGetToken("history-admin@example.com", "admin123");

        mockMvc.perform(get("/api/students/history")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/students/history")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/students/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminAnalyticsShouldAllowAdminAndReturnExpectedMetrics() throws Exception {
        AppUser studentA = new AppUser();
        studentA.setEmail("analytics-student-a@example.com");
        studentA.setPassword(passwordEncoder.encode("pass123"));
        studentA.setRole(Role.STUDENT);
        studentA.setEnabled(true);
        appUserRepository.save(studentA);

        AppUser studentB = new AppUser();
        studentB.setEmail("analytics-student-b@example.com");
        studentB.setPassword(passwordEncoder.encode("pass123"));
        studentB.setRole(Role.STUDENT);
        studentB.setEnabled(true);
        appUserRepository.save(studentB);

        StudentProfile readyProfile = new StudentProfile();
        readyProfile.setUser(studentA);
        readyProfile.setCgpa(8.5);
        readyProfile.setDsaRating(8);
        readyProfile.setProjectsCount(3);
        readyProfile.setInternship(true);
        readyProfile.setAttendance(88);
        readyProfile.setAptitudeScore(84);
        readyProfile.setReadinessScore(0.8);
        readyProfile.setReadinessLabel("Likely Placed");
        studentProfileRepository.save(readyProfile);

        StudentProfile notReadyProfile = new StudentProfile();
        notReadyProfile.setUser(studentB);
        notReadyProfile.setCgpa(6.8);
        notReadyProfile.setDsaRating(4);
        notReadyProfile.setProjectsCount(1);
        notReadyProfile.setInternship(false);
        notReadyProfile.setAttendance(70);
        notReadyProfile.setAptitudeScore(45);
        notReadyProfile.setReadinessScore(0.2);
        notReadyProfile.setReadinessLabel(null);
        studentProfileRepository.save(notReadyProfile);

        AppUser admin = new AppUser();
        admin.setEmail("analytics-admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        appUserRepository.save(admin);
        String adminToken = loginAndGetToken("analytics-admin@example.com", "admin123");

        mockMvc.perform(get("/api/admin/analytics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(2))
                .andExpect(jsonPath("$.readyStudentsCount").value(1))
                .andExpect(jsonPath("$.notReadyStudentsCount").value(1))
                .andExpect(jsonPath("$.averageReadinessScore").value(0.5));
    }

    @Test
    void adminAnalyticsShouldRejectStudentAndInvalidToken() throws Exception {
        String studentToken = registerAndGetToken("analytics-student@example.com", "password123");

        mockMvc.perform(get("/api/admin/analytics")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/analytics")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/analytics"))
                .andExpect(status().isUnauthorized());
    }

    private String registerAndGetToken(String email, String password) throws Exception {
        String body = String.format("""
                {
                  "email":"%s",
                  "password":"%s"
                }
                """, email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String body = String.format("""
                {
                  "email":"%s",
                  "password":"%s"
                }
                """, email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }
}
