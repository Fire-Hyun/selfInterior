package com.selfinterior.api.visualqa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisualQuestionImageRepository
    extends JpaRepository<VisualQuestionImageEntity, UUID> {
  List<VisualQuestionImageEntity> findByQuestionIdOrderByCreatedAtAsc(UUID questionId);
}
