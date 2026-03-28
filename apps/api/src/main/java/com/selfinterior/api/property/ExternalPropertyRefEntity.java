package com.selfinterior.api.property;

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
@Table(name = "external_property_refs")
public class ExternalPropertyRefEntity extends AuditableEntity {
  @Column(name = "property_id", nullable = false)
  private UUID propertyId;

  @Column(nullable = false)
  private String provider;

  @Column(name = "external_key", nullable = false)
  private String externalKey;

  @Column(name = "ref_type", nullable = false)
  private String refType;

  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> metadata;
}
