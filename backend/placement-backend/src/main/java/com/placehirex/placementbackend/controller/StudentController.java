package com.placehirex.placementbackend.controller;

import com.placehirex.placementbackend.dto.PredictionRequest;
import com.placehirex.placementbackend.model.Student;
import com.placehirex.placementbackend.repository.StudentRepository;
import com.placehirex.placementbackend.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PredictionService predictionService;

    @PostMapping("/predict")
    public Student predictPlacement(@RequestBody Student student) {

        // Prepare ML request
        PredictionRequest request = new PredictionRequest();
        request.setCgpa(student.getCgpa());
        request.setDsa_rating(student.getDsaRating());
        request.setProjects(student.getProjects());
        request.setInternship(student.isInternship() ? 1 : 0);
        request.setAttendance(student.getAttendance());
        request.setAptitude_score(student.getAptitudeScore());

        // Call ML API
        Map<String, Object> response = predictionService.getPrediction(request);

        // Set prediction result
        student.setPredictionScore(
                Double.parseDouble(response.get("score").toString())
        );
        student.setStatus(response.get("status").toString());

        // Save and return
        return studentRepository.save(student);
    }
}
