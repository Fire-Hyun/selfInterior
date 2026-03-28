package com.selfinterior.api.user;

import com.selfinterior.api.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity extends AuditableEntity {
  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(nullable = false)
  private String provider;

  @Column(nullable = false)
  private String name;

  private String phone;

  @Column(nullable = false)
  private String role;

  @Column(name = "marketing_consent", nullable = false)
  private boolean marketingConsent;

  @Column(name = "last_login_at")
  private OffsetDateTime lastLoginAt;
}
