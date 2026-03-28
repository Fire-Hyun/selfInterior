package com.selfinterior.api.floorplan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FloorPlanCandidateRepository
    extends JpaRepository<FloorPlanCandidateEntity, UUID> {
  List<FloorPlanCandidateEntity> findByProjectIdOrderByConfidenceScoreDesc(UUID projectId);

  List<FloorPlanCandidateEntity> findByProjectId(UUID projectId);

  Optional<FloorPlanCandidateEntity> findByProjectIdAndId(UUID projectId, UUID id);

  Optional<FloorPlanCandidateEntity> findByProjectIdAndSelectedTrue(UUID projectId);
}
