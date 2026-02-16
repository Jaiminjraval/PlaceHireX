package com.placehirex.placementbackend.repository;

import com.placehirex.placementbackend.model.StudentProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    List<StudentProfile> findByUserEmail(String email);
    Optional<StudentProfile> findFirstByUserEmail(String email);
}
