package com.selfinterior.api.expert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "expert_lead_events")
public class ExpertLeadEventEntity {
  @Id @GeneratedValue private UUID id;

  @Column(name = "expert_lead_id", nullable = false)
  private UUID expertLeadId;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  private LeadEventType eventType;

  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> payload;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;
}
