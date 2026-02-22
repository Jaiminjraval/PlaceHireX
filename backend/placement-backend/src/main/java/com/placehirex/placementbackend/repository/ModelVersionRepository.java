package com.placehirex.placementbackend.repository;

import com.placehirex.placementbackend.model.ModelVersion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelVersionRepository extends JpaRepository<ModelVersion, Long> {
    Optional<ModelVersion> findByIsActiveTrue();

    Optional<ModelVersion> findByModelName(String modelName);
}
