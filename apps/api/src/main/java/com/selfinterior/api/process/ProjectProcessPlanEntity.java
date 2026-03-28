package com.selfinterior.api.process;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "project_process_plans")
public class ProjectProcessPlanEntity extends AuditableEntity {
  @Column(name = "project_id", nullable = false, unique = true)
  private UUID projectId;

  @Enumerated(EnumType.STRING)
  @Column(name = "plan_status", nullable = false)
  private ProcessPlanStatus planStatus;

  @Column(name = "generated_from_floor_plan_id", nullable = false)
  private UUID generatedFromFloorPlanId;

  @Column(name = "current_step_key")
  private String currentStepKey;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "generated_summary")
  private Map<String, Object> generatedSummary;
}
