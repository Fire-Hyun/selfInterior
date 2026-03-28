package com.selfinterior.api.project;

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
@Table(name = "projects")
public class ProjectEntity extends AuditableEntity {
  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Column(nullable = false)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProjectStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "project_type", nullable = false)
  private ProjectType projectType;

  @Enumerated(EnumType.STRING)
  @Column(name = "living_status", nullable = false)
  private LivingStatus livingStatus;

  @Column(name = "budget_min")
  private Integer budgetMin;

  @Column(name = "budget_max")
  private Integer budgetMax;

  @Column(nullable = false)
  private String currency;

  @Column(name = "current_process_step")
  private String currentProcessStep;

  @Column(name = "onboarding_completed", nullable = false)
  private boolean onboardingCompleted;
}
