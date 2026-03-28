package com.selfinterior.api.style;

import com.selfinterior.api.common.persistence.AuditableEntity;
import com.selfinterior.api.visualqa.SpaceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "project_style_selections")
public class ProjectStyleSelectionEntity extends AuditableEntity {
  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "style_preset_id", nullable = false)
  private UUID stylePresetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "space_type", nullable = false)
  private SpaceType spaceType;

  @Column(nullable = false)
  private int priority;

  @Column(nullable = false)
  private boolean selected;
}
