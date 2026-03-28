package com.selfinterior.api.expert;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "expert_portfolios")
public class ExpertPortfolioEntity extends AuditableEntity {
  @Column(name = "expert_id", nullable = false)
  private UUID expertId;

  @Column(nullable = false)
  private String title;

  private String description;

  @Column(name = "storage_key")
  private String storageKey;

  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> metadata;
}
