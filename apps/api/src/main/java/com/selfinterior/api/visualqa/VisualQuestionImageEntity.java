package com.selfinterior.api.visualqa;

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
@Table(name = "visual_question_images")
public class VisualQuestionImageEntity extends AuditableEntity {
  @Column(name = "question_id", nullable = false)
  private UUID questionId;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "storage_path", nullable = false)
  private String storagePath;
}
