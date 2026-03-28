package com.selfinterior.api.style;

import com.selfinterior.api.common.persistence.AuditableEntity;
import com.selfinterior.api.visualqa.SpaceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "generated_style_images")
public class GeneratedStyleImageEntity extends AuditableEntity {
  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "style_preset_id")
  private UUID stylePresetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "space_type", nullable = false)
  private SpaceType spaceType;

  @Column(name = "prompt_text", nullable = false)
  private String promptText;

  @Column(name = "negative_prompt_text")
  private String negativePromptText;

  @Enumerated(EnumType.STRING)
  @Column(name = "generation_status", nullable = false)
  private StyleGenerationStatus generationStatus;

  @Column(name = "storage_key")
  private String storageKey;

  @Column(name = "thumbnail_key")
  private String thumbnailKey;

  private String seed;

  @Column(name = "model_name")
  private String modelName;

  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> metadata;

  @Column(nullable = false)
  private boolean liked;
}
