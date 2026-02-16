package com.placehirex.placementbackend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.placehirex.placementbackend.model.AppUser;
import com.placehirex.placementbackend.model.Role;
import com.placehirex.placementbackend.model.StudentProfile;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class StudentProfileRepositoryTest {
    @Autowired
    private StudentProfileRepository studentProfileRepository;
    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void shouldFindProfilesByUserEmail() {
        AppUser user = new AppUser();
        user.setEmail("repo-student@example.com");
        user.setPassword("hashed");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        AppUser savedUser = appUserRepository.save(user);

        StudentProfile profile = new StudentProfile();
        profile.setUser(savedUser);
        profile.setCgpa(8.4);
        profile.setDsaRating(7);
        profile.setProjectsCount(2);
        profile.setInternship(true);
        profile.setAttendance(88.5);
        profile.setAptitudeScore(82.0);
        profile.setReadinessScore(0.81);
        profile.setReadinessLabel("Likely Placed");
        studentProfileRepository.save(profile);

        List<StudentProfile> profiles = studentProfileRepository.findByUserEmail("repo-student@example.com");
        assertEquals(1, profiles.size());
        assertEquals("repo-student@example.com", profiles.get(0).getUser().getEmail());
    }
}
