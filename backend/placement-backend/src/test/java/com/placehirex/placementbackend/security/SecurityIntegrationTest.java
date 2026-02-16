package com.placehirex.placementbackend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placehirex.placementbackend.dto.PredictionResponse;
import com.placehirex.placementbackend.model.AppUser;
import com.placehirex.placementbackend.model.Role;
import com.placehirex.placementbackend.model.StudentProfile;
import com.placehirex.placementbackend.repository.AppUserRepository;
import com.placehirex.placementbackend.repository.StudentProfileRepository;
import com.placehirex.placementbackend.service.PredictionService;
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
    private PasswordEncoder passwordEncoder;

    @MockBean
    private PredictionService predictionService;

    @BeforeEach
    void setUp() {
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
        when(predictionService.getPrediction(any())).thenReturn(new PredictionResponse(0.78, "Likely Placed"));

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
                  "projectsCount":3,
                  "internship":true,
                  "attendance":90.0,
                  "aptitudeScore":85.0
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
                .andExpect(jsonPath("$.readinessScore").value(0.78))
                .andExpect(jsonPath("$.readinessLabel").value("Likely Placed"));

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
    }

    @Test
    void studentsPredictShouldFailWhenProfileMissing() throws Exception {
        String studentToken = registerAndGetToken("student5@example.com", "password123");

        mockMvc.perform(post("/api/students/predict")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
