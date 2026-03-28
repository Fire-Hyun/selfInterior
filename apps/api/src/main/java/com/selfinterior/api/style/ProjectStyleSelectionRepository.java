package com.selfinterior.api.style;

import com.selfinterior.api.visualqa.SpaceType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectStyleSelectionRepository
    extends JpaRepository<ProjectStyleSelectionEntity, UUID> {
  List<ProjectStyleSelectionEntity> findByProjectIdAndSpaceType(
      UUID projectId, SpaceType spaceType);
}
