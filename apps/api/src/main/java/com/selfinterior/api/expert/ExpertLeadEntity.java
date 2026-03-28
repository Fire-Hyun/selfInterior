package com.selfinterior.api.expert;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "expert_leads")
public class ExpertLeadEntity extends AuditableEntity {
  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "expert_id", nullable = false)
  private UUID expertId;

  @Column(name = "requested_category_id", nullable = false)
  private UUID requestedCategoryId;

  @Enumerated(EnumType.STRING)
  @Column(name = "lead_status", nullable = false)
  private LeadStatus leadStatus;

  @Column(name = "budget_min")
  private Integer budgetMin;

  @Column(name = "budget_max")
  private Integer budgetMax;

  @Column(name = "desired_start_date")
  private LocalDate desiredStartDate;

  private String message;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "attachment_payload")
  private Map<String, Object> attachmentPayload;

  @Column(name = "created_by_user_id", nullable = false)
  private UUID createdByUserId;
}
