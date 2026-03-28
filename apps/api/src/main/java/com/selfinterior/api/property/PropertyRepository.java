package com.selfinterior.api.property;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<PropertyEntity, UUID> {
  Optional<PropertyEntity> findByProjectId(UUID projectId);
}
