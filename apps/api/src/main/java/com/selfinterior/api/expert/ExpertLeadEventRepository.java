package com.selfinterior.api.expert;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertLeadEventRepository extends JpaRepository<ExpertLeadEventEntity, UUID> {
  List<ExpertLeadEventEntity> findByExpertLeadIdOrderByCreatedAtAsc(UUID expertLeadId);
}
