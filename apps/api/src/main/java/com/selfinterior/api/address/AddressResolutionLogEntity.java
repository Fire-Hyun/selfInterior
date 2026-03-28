package com.selfinterior.api.address;

import com.selfinterior.api.common.persistence.CreatedOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "address_resolution_logs")
public class AddressResolutionLogEntity extends CreatedOnlyEntity {
  @Column(name = "project_id")
  private UUID projectId;

  @Column(name = "input_query", nullable = false)
  private String inputQuery;

  @Column(name = "normalized_road_address")
  private String normalizedRoadAddress;

  @Column(name = "normalized_jibun_address")
  private String normalizedJibunAddress;

  @Column(name = "road_code")
  private String roadCode;

  @Column(name = "building_main_no")
  private String buildingMainNo;

  @Column(name = "building_sub_no")
  private String buildingSubNo;

  @Column(name = "legal_dong_code")
  private String legalDongCode;

  @Column(nullable = false)
  private String source;

  @Column(name = "confidence_score", nullable = false)
  private BigDecimal confidenceScore;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "raw_payload", nullable = false)
  private Map<String, Object> rawPayload;
}
