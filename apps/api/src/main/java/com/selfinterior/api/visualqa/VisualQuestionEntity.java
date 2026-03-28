package com.selfinterior.api.visualqa;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "visual_questions")
public class VisualQuestionEntity extends AuditableEntity {
  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "question_text", nullable = false)
  private String questionText;

  @Column(name = "process_step_key")
  private String processStepKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "space_type", nullable = false)
  private SpaceType spaceType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VisualQuestionStatus status;
}
