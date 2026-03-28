package com.selfinterior.api.visualqa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisualQuestionRepository extends JpaRepository<VisualQuestionEntity, UUID> {
  List<VisualQuestionEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
