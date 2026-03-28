package com.selfinterior.api.project;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "project_scopes")
public class ProjectScopeEntity extends AuditableEntity {
  @Column(name = "project_id", nullable = false, unique = true)
  private UUID projectId;

  @Enumerated(EnumType.STRING)
  @Column(name = "scope_type", nullable = false)
  private ProjectType scopeType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "spaces_targeted")
  private List<String> spacesTargeted;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "keep_items")
  private List<String> keepItems;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "self_work_items")
  private List<String> selfWorkItems;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "desired_style_keywords")
  private List<String> desiredStyleKeywords;

  @Column(name = "schedule_start_target")
  private LocalDate scheduleStartTarget;

  @Column(name = "schedule_end_target")
  private LocalDate scheduleEndTarget;

  @Column(name = "special_notes")
  private String specialNotes;
}
