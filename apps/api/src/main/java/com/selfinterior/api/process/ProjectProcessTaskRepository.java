package com.selfinterior.api.process;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectProcessTaskRepository
    extends JpaRepository<ProjectProcessTaskEntity, UUID> {
  List<ProjectProcessTaskEntity> findByProjectProcessStepIdOrderByItemOrderAsc(
      UUID projectProcessStepId);
}
