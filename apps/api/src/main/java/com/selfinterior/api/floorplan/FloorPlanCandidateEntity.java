package com.selfinterior.api.floorplan;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "floor_plan_candidates")
public class FloorPlanCandidateEntity extends AuditableEntity {
  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "property_id", nullable = false)
  private UUID propertyId;

  @Column(name = "floor_plan_source_id", nullable = false)
  private UUID floorPlanSourceId;

  @Column(name = "provider_plan_key")
  private String providerPlanKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false)
  private FloorPlanSourceType sourceType;

  @Column(nullable = false)
  private String source;

  @Enumerated(EnumType.STRING)
  @Column(name = "match_type", nullable = false)
  private MatchType matchType;

  @Column(name = "confidence_score", nullable = false)
  private BigDecimal confidenceScore;

  @Enumerated(EnumType.STRING)
  @Column(name = "confidence_grade", nullable = false)
  private ConfidenceGrade confidenceGrade;

  @Column(name = "exclusive_area_m2")
  private BigDecimal exclusiveAreaM2;

  @Column(name = "supply_area_m2")
  private BigDecimal supplyAreaM2;

  @Column(name = "room_count")
  private Integer roomCount;

  @Column(name = "bathroom_count")
  private Integer bathroomCount;

  @Column(name = "layout_label")
  private String layoutLabel;

  @Column(name = "is_selected", nullable = false)
  private boolean selected;

  @Column(name = "selection_reason")
  private String selectionReason;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "raw_payload")
  private Map<String, Object> rawPayload;

  @Column(name = "raw_payload_ref")
  private String rawPayloadRef;

  @Column(name = "normalized_plan_ref")
  private String normalizedPlanRef;
}
