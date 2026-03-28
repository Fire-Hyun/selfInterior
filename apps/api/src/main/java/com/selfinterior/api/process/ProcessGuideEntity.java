package com.selfinterior.api.process;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "process_guides")
public class ProcessGuideEntity extends AuditableEntity {
  @Column(name = "process_catalog_id", nullable = false, unique = true)
  private UUID processCatalogId;

  @Column(name = "purpose_text", nullable = false)
  private String purposeText;

  @Column(name = "start_check_intro", nullable = false)
  private String startCheckIntro;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "decision_points", nullable = false)
  private List<String> decisionPoints;

  @Column(name = "self_work_text", nullable = false)
  private String selfWorkText;

  @Column(name = "expert_required_text", nullable = false)
  private String expertRequiredText;

  @Column(name = "mistakes_text", nullable = false)
  private String mistakesText;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "next_step_checks", nullable = false)
  private List<String> nextStepChecks;
}
