package com.selfinterior.api.expert;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "expert_category_links")
public class ExpertCategoryLinkEntity extends AuditableEntity {
  @Column(name = "expert_id", nullable = false)
  private UUID expertId;

  @Column(name = "expert_category_id", nullable = false)
  private UUID expertCategoryId;
}
