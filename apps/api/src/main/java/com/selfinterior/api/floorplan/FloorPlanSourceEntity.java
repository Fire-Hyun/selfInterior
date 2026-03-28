package com.selfinterior.api.floorplan;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "floor_plan_sources")
public class FloorPlanSourceEntity extends AuditableEntity {
  @Column(nullable = false)
  private String provider;

  @Enumerated(EnumType.STRING)
  @Column(name = "license_status", nullable = false)
  private LicenseStatus licenseStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "access_scope", nullable = false)
  private AccessScope accessScope;

  @Column(name = "provider_doc_ref")
  private String providerDocRef;
}
