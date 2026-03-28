package com.selfinterior.api.visualqa;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "visual_answers")
public class VisualAnswerEntity extends AuditableEntity {
  @Column(name = "question_id", nullable = false, unique = true)
  private UUID questionId;

  @Enumerated(EnumType.STRING)
  @Column(name = "risk_level", nullable = false)
  private RiskLevel riskLevel;

  @Column(name = "observed_text", nullable = false)
  private String observedText;

  @Column(name = "possible_causes_text", nullable = false)
  private String possibleCausesText;

  @Column(name = "next_checks_text", nullable = false)
  private String nextChecksText;

  @Column(name = "proceed_recommendation_text", nullable = false)
  private String proceedRecommendationText;

  @Column(name = "expert_required", nullable = false)
  private boolean expertRequired;

  @Column(name = "confidence_score", nullable = false)
  private BigDecimal confidenceScore;
}
