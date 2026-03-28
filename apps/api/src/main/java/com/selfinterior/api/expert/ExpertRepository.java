package com.selfinterior.api.expert;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertRepository extends JpaRepository<ExpertEntity, UUID> {
  List<ExpertEntity> findByStatus(ExpertStatus status);
}
