package com.selfinterior.api.process;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "project_process_tasks")
public class ProjectProcessTaskEntity extends AuditableEntity {
  @Column(name = "project_process_step_id", nullable = false)
  private UUID projectProcessStepId;

  @Enumerated(EnumType.STRING)
  @Column(name = "task_group", nullable = false)
  private ProcessTaskGroup taskGroup;

  @Column(name = "item_order", nullable = false)
  private Integer itemOrder;

  @Column(nullable = false)
  private String title;

  @Column private String description;

  @Column(nullable = false)
  private boolean completed;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;
}
