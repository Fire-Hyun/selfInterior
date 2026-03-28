package com.selfinterior.api.process;

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
@Table(name = "project_process_steps")
public class ProjectProcessStepEntity extends AuditableEntity {
  @Column(name = "process_plan_id", nullable = false)
  private UUID processPlanId;

  @Column(name = "process_catalog_id", nullable = false)
  private UUID processCatalogId;

  @Column(name = "step_key", nullable = false)
  private String stepKey;

  @Column(nullable = false)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProcessStepStatus status;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder;

  @Column(name = "duration_days", nullable = false)
  private Integer durationDays;

  @Column(name = "is_required", nullable = false)
  private boolean required;

  @Column(nullable = false)
  private String description;
}
