package com.selfinterior.api.expert;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertServiceRegionRepository
    extends JpaRepository<ExpertServiceRegionEntity, UUID> {
  List<ExpertServiceRegionEntity> findByExpertIdIn(List<UUID> expertIds);

  List<ExpertServiceRegionEntity> findByExpertId(UUID expertId);
}
