package com.placehirex.placementbackend.repository;

import com.placehirex.placementbackend.model.PredictionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredictionHistoryRepository extends JpaRepository<PredictionHistory, Long> {
    List<PredictionHistory> findTop10ByStudentProfileUserEmailOrderByTimestampDesc(String email);
}
