package com.selfinterior.api.process;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectProcessPlanRepository
    extends JpaRepository<ProjectProcessPlanEntity, UUID> {
  Optional<ProjectProcessPlanEntity> findByProjectId(UUID projectId);
}
