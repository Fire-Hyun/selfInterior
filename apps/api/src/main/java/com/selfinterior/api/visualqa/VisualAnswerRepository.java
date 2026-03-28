package com.selfinterior.api.visualqa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisualAnswerRepository extends JpaRepository<VisualAnswerEntity, UUID> {
  Optional<VisualAnswerEntity> findByQuestionId(UUID questionId);
}
