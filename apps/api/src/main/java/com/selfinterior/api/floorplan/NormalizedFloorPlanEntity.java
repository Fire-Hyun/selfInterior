package com.selfinterior.api.floorplan;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "normalized_floor_plans")
public class NormalizedFloorPlanEntity extends AuditableEntity {
  @Column(name = "floor_plan_candidate_id", nullable = false, unique = true)
  private UUID floorPlanCandidateId;

  @Enumerated(EnumType.STRING)
  @Column(name = "normalization_status", nullable = false)
  private NormalizationStatus normalizationStatus;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "plan_json", nullable = false)
  private Map<String, Object> planJson;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "uncertainty_json")
  private Map<String, Object> uncertaintyJson;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "manual_check_items")
  private List<String> manualCheckItems;

  @Enumerated(EnumType.STRING)
  @Column(name = "normalized_by", nullable = false)
  private NormalizedBy normalizedBy;
}
