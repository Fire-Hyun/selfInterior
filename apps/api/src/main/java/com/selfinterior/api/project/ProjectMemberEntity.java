package com.selfinterior.api.project;

import com.selfinterior.api.common.persistence.AuditableEntity;
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
@Table(name = "project_members")
public class ProjectMemberEntity extends AuditableEntity {
  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "member_role", nullable = false)
  private MemberRole memberRole;
}
