package com.placehirex.placementbackend.controller;

import com.placehirex.placementbackend.dto.PredictionRequest;
import com.placehirex.placementbackend.dto.PredictionResponse;
import com.placehirex.placementbackend.model.AppUser;
import com.placehirex.placementbackend.model.StudentProfile;
import com.placehirex.placementbackend.repository.AppUserRepository;
import com.placehirex.placementbackend.repository.StudentProfileRepository;
import com.placehirex.placementbackend.service.PredictionService;
import java.util.NoSuchElementException;
import java.util.HashMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {
    private final StudentProfileRepository studentProfileRepository;
    private final AppUserRepository appUserRepository;
    private final PredictionService predictionService;

    public StudentController(
            StudentProfileRepository studentProfileRepository,
            AppUserRepository appUserRepository,
            PredictionService predictionService
    ) {
        this.studentProfileRepository = studentProfileRepository;
        this.appUserRepository = appUserRepository;
        this.predictionService = predictionService;
    }

    @PostMapping("/profile")
    public StudentProfile upsertProfile(@RequestBody StudentProfile requestProfile, Authentication authentication) {
        AppUser appUser = getAuthenticatedUser(authentication);

        StudentProfile profile = studentProfileRepository.findFirstByUserEmail(appUser.getEmail())
                .orElseGet(StudentProfile::new);

        profile.setUser(appUser);
        profile.setCgpa(requestProfile.getCgpa());
        profile.setDsaRating(requestProfile.getDsaRating());
        profile.setProjectsCount(requestProfile.getProjectsCount());
        profile.setInternship(requestProfile.isInternship());
        profile.setAttendance(requestProfile.getAttendance());
        profile.setAptitudeScore(requestProfile.getAptitudeScore());

        return studentProfileRepository.save(profile);
    }

    @GetMapping("/profile")
    public StudentProfile getProfile(Authentication authentication) {
        AppUser appUser = getAuthenticatedUser(authentication);
        return studentProfileRepository.findFirstByUserEmail(appUser.getEmail())
                .orElseThrow(() -> new NoSuchElementException("Student profile not found"));
    }

    @PostMapping("/predict")
    public Map<String, Object> predictPlacement(Authentication authentication) {
        AppUser appUser = getAuthenticatedUser(authentication);
        StudentProfile studentProfile = studentProfileRepository.findFirstByUserEmail(appUser.getEmail())
                .orElseThrow(() -> new NoSuchElementException("Student profile not found"));

        // Prepare ML request
        PredictionRequest request = new PredictionRequest();
        request.setCgpa(studentProfile.getCgpa());
        request.setDsaRating(studentProfile.getDsaRating());
        request.setProjectsCount(studentProfile.getProjectsCount());
        request.setInternship(studentProfile.isInternship());
        request.setAttendance(studentProfile.getAttendance());
        request.setAptitudeScore(studentProfile.getAptitudeScore());

        // Call ML API
        PredictionResponse response = predictionService.getPrediction(request);

        // Set prediction result
        studentProfile.setReadinessScore(response.getProbability());
        studentProfile.setReadinessLabel(response.getLabel());
        studentProfile.setUser(appUser);

        StudentProfile savedProfile = studentProfileRepository.save(studentProfile);

        Map<String, Object> predictionResult = new HashMap<>();
        predictionResult.put("readinessScore", savedProfile.getReadinessScore());
        predictionResult.put("readinessLabel", savedProfile.getReadinessLabel());
        return predictionResult;
    }

    private AppUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BadCredentialsException("Authentication is required");
        }
        String userEmail = authentication.getName();
        return appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found"));
    }
}
