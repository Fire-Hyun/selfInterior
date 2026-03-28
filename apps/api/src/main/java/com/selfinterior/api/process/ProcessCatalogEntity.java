package com.selfinterior.api.process;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "process_catalogs")
public class ProcessCatalogEntity extends AuditableEntity {
  @Column(name = "step_key", nullable = false, unique = true)
  private String stepKey;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String description;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder;

  @Column(name = "default_duration_days", nullable = false)
  private Integer defaultDurationDays;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "applicable_project_types", nullable = false)
  private List<String> applicableProjectTypes;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "applicable_living_statuses", nullable = false)
  private List<String> applicableLivingStatuses;
}
