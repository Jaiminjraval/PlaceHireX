package com.placehirex.placementbackend.controller;

import com.placehirex.placementbackend.model.ModelVersion;
import com.placehirex.placementbackend.repository.ModelVersionRepository;
import com.placehirex.placementbackend.service.PredictionService;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/models")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModelController {

    private final ModelVersionRepository modelVersionRepository;
    private final PredictionService predictionService;

    public AdminModelController(ModelVersionRepository modelVersionRepository,
            PredictionService predictionService) {
        this.modelVersionRepository = modelVersionRepository;
        this.predictionService = predictionService;
    }

    @GetMapping
    public List<ModelVersion> getAllModels() {
        return modelVersionRepository.findAll();
    }

    @PostMapping("/select")
    public ResponseEntity<?> selectModel(@RequestParam("id") Long id) {
        Optional<ModelVersion> modelOptional = modelVersionRepository.findById(id);
        if (modelOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Model not found");
        }

        ModelVersion selectedModel = modelOptional.get();

        // Deactivate all
        List<ModelVersion> allModels = modelVersionRepository.findAll();
        for (ModelVersion model : allModels) {
            model.setActive(false);
        }

        // Activate selected
        selectedModel.setActive(true);
        modelVersionRepository.saveAll(allModels); // Save changes

        return ResponseEntity.ok("Model " + selectedModel.getModelName() + " activated successfully");
    }

    @PatchMapping("/add")
    public ResponseEntity<?> addModel(@RequestBody ModelVersion modelVersion) {
        if (modelVersionRepository.findByModelName(modelVersion.getModelName()).isPresent()) {
            return ResponseEntity.badRequest().body("Model with this name already exists");
        }

        modelVersion.setUploadedAt(LocalDateTime.now());
        if (modelVersion.isActive()) {
            // If adding as active, deactivate others
            List<ModelVersion> allModels = modelVersionRepository.findAll();
            for (ModelVersion model : allModels) {
                model.setActive(false);
            }
            modelVersionRepository.saveAll(allModels);
        }

        ModelVersion saved = modelVersionRepository.save(modelVersion);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/upload-dataset")
    public ResponseEntity<?> uploadDataset(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }

        try {
            // Create a temporary file
            File tempFile = File.createTempFile("dataset-", ".csv");
            file.transferTo(tempFile);

            // Call PredictionService to retrain
            String response = predictionService.retrainModel(new FileSystemResource(tempFile));

            // Delete temp file
            boolean deleted = tempFile.delete();
            if (!deleted) {
                System.err.println("Failed to delete temp file: " + tempFile.getAbsolutePath());
            }

            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to store file: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Retraining failed: " + ex.getMessage());
        }
    }
}
