package com.selfinterior.api.expert;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "experts")
public class ExpertEntity extends AuditableEntity {
  @Column(name = "company_name", nullable = false)
  private String companyName;

  @Column(name = "contact_name", nullable = false)
  private String contactName;

  private String phone;
  private String email;

  @Column(name = "business_no")
  private String businessNo;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "license_info")
  private Map<String, Object> licenseInfo;

  @Column(name = "intro_text")
  private String introText;

  @Column(name = "min_budget")
  private Integer minBudget;

  @Column(name = "max_budget")
  private Integer maxBudget;

  @Column(name = "partial_work_supported", nullable = false)
  private boolean partialWorkSupported;

  @Column(name = "semi_self_collaboration_supported", nullable = false)
  private boolean semiSelfCollaborationSupported;

  @Column(name = "response_score")
  private BigDecimal responseScore;

  @Column(name = "review_score")
  private BigDecimal reviewScore;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ExpertStatus status;
}
