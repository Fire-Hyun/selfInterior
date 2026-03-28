package com.selfinterior.api.property;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "properties")
public class PropertyEntity extends AuditableEntity {
  @Column(name = "project_id", nullable = false, unique = true)
  private UUID projectId;

  @Enumerated(EnumType.STRING)
  @Column(name = "property_type", nullable = false)
  private PropertyType propertyType;

  @Column(name = "country_code", nullable = false)
  private String countryCode;

  private String sido;
  private String sigungu;

  @Column(name = "eup_myeon_dong")
  private String eupMyeonDong;

  @Column(name = "road_address")
  private String roadAddress;

  @Column(name = "jibun_address")
  private String jibunAddress;

  @Column(name = "detail_address")
  private String detailAddress;

  @Column(name = "apartment_name")
  private String apartmentName;

  @Column(name = "building_no")
  private String buildingNo;

  @Column(name = "dong_no")
  private String dongNo;

  @Column(name = "ho_no")
  private String hoNo;

  @Column(name = "postal_code")
  private String postalCode;

  private BigDecimal lat;
  private BigDecimal lng;

  @Column(name = "completion_year")
  private Integer completionYear;

  @Column(name = "approval_date")
  private LocalDate approvalDate;

  @Column(name = "household_count")
  private Integer householdCount;

  @Column(name = "supply_area_m2")
  private BigDecimal supplyAreaM2;

  @Column(name = "exclusive_area_m2")
  private BigDecimal exclusiveAreaM2;

  @Column(name = "room_count")
  private Integer roomCount;

  @Column(name = "bathroom_count")
  private Integer bathroomCount;

  @Column(name = "balcony_count")
  private Integer balconyCount;

  @Column(name = "heating_type")
  private String heatingType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "raw_summary")
  private Map<String, Object> rawSummary;
}
