package com.selfinterior.api.style;

import com.selfinterior.api.visualqa.SpaceType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedStyleImageRepository
    extends JpaRepository<GeneratedStyleImageEntity, UUID> {
  List<GeneratedStyleImageEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

  List<GeneratedStyleImageEntity> findByProjectIdAndSpaceType(UUID projectId, SpaceType spaceType);
}
