package com.selfinterior.api.process;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectProcessStepRepository
    extends JpaRepository<ProjectProcessStepEntity, UUID> {
  List<ProjectProcessStepEntity> findByProcessPlanIdOrderBySortOrderAsc(UUID processPlanId);

  Optional<ProjectProcessStepEntity> findByProcessPlanIdAndStepKey(
      UUID processPlanId, String stepKey);
}
