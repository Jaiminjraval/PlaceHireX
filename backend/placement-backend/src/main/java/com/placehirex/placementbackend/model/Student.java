package com.placehirex.placementbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password; // hashed password

    private double cgpa;
    private int dsaRating;
    private int projects;
    private boolean internship;
    private int attendance;
    private int aptitudeScore;

    private double predictionScore;
    private String status;

    @Transient
    private List<String> explanations;
}
