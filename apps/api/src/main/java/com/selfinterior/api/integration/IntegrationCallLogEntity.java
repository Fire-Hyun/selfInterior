package com.selfinterior.api.integration;

import com.selfinterior.api.common.persistence.CreatedOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "integration_call_logs")
public class IntegrationCallLogEntity extends CreatedOnlyEntity {
  @Column(nullable = false)
  private String provider;

  @Column(nullable = false)
  private String operation;

  @Column(name = "request_id")
  private String requestId;

  @Column(name = "status_code")
  private String statusCode;

  @Column(nullable = false)
  private boolean success;

  @Column(name = "latency_ms")
  private Integer latencyMs;

  @Column(name = "error_message")
  private String errorMessage;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "request_meta")
  private Map<String, Object> requestMeta;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "response_meta")
  private Map<String, Object> responseMeta;
}
