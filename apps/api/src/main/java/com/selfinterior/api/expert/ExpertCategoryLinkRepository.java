package com.selfinterior.api.expert;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertCategoryLinkRepository
    extends JpaRepository<ExpertCategoryLinkEntity, UUID> {
  List<ExpertCategoryLinkEntity> findByExpertIdIn(List<UUID> expertIds);

  List<ExpertCategoryLinkEntity> findByExpertId(UUID expertId);
}
