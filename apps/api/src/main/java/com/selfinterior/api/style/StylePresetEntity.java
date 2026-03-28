package com.selfinterior.api.style;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "style_presets")
public class StylePresetEntity extends AuditableEntity {
  @Column(nullable = false, unique = true)
  private String key;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(name = "prompt_template", nullable = false)
  private String promptTemplate;

  @Column(nullable = false)
  private boolean active;
}
