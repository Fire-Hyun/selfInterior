package com.selfinterior.api.floorplan;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NormalizedFloorPlanRepository
    extends JpaRepository<NormalizedFloorPlanEntity, UUID> {
  Optional<NormalizedFloorPlanEntity> findByFloorPlanCandidateId(UUID floorPlanCandidateId);
}
