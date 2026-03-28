package com.selfinterior.api.project;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectScopeRepository extends JpaRepository<ProjectScopeEntity, UUID> {
  Optional<ProjectScopeEntity> findByProjectId(UUID projectId);
}
