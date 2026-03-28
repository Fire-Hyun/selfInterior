package com.selfinterior.api.expert;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertLeadRepository extends JpaRepository<ExpertLeadEntity, UUID> {
  List<ExpertLeadEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
