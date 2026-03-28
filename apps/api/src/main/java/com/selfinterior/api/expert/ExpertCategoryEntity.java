package com.selfinterior.api.expert;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "expert_categories")
public class ExpertCategoryEntity extends AuditableEntity {
  @Column(nullable = false, unique = true)
  private String key;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private boolean active;
}
