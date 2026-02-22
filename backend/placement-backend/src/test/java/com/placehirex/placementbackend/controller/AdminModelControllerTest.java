package com.placehirex.placementbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.placehirex.placementbackend.model.ModelVersion;
import com.placehirex.placementbackend.repository.ModelVersionRepository;
import com.placehirex.placementbackend.security.JwtAuthenticationFilter;
import com.placehirex.placementbackend.security.RestAccessDeniedHandler;
import com.placehirex.placementbackend.security.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminModelController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ModelVersionRepository modelVersionRepository;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @MockBean
    private com.placehirex.placementbackend.service.PredictionService predictionService;

    @Test
    void getAllModels_ShouldReturnList() throws Exception {
        ModelVersion v1 = new ModelVersion(1L, "v1", 0.9, true, null, "desc");
        given(modelVersionRepository.findAll()).willReturn(Collections.singletonList(v1));

        mockMvc.perform(get("/api/admin/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].modelName").value("v1"));
    }

    @Test
    void selectModel_ShouldActivateModelAndDeactivateOthers() throws Exception {
        ModelVersion v1 = new ModelVersion(1L, "v1", 0.9, true, null, "desc");
        ModelVersion v2 = new ModelVersion(2L, "v2", 0.95, false, null, "desc");

        given(modelVersionRepository.findById(2L)).willReturn(Optional.of(v2));
        given(modelVersionRepository.findAll()).willReturn(Arrays.asList(v1, v2));

        mockMvc.perform(post("/api/admin/models/select")
                .param("id", "2"))
                .andExpect(status().isOk());

        // Verify logic: v1 should be inactive, v2 active
        assert (!v1.isActive());
        assert (v2.isActive());
        verify(modelVersionRepository).saveAll(any());
    }

    @Test
    void addModel_ShouldAddModel() throws Exception {
        ModelVersion newModel = new ModelVersion(null, "v3", 0.88, false, null, "new model");
        given(modelVersionRepository.findByModelName("v3")).willReturn(Optional.empty());
        given(modelVersionRepository.save(any(ModelVersion.class))).willReturn(newModel);

        mockMvc.perform(patch("/api/admin/models/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newModel)))
                .andExpect(status().isOk());
    }

    @Test
    void uploadDataset_ShouldCallPredictionService() throws Exception {
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file", "data.csv", "text/csv", "content".getBytes());
        given(predictionService.retrainModel(any())).willReturn("Training started");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .multipart("/api/admin/models/upload-dataset")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string("Training started"));

        verify(predictionService).retrainModel(any());
    }
}
