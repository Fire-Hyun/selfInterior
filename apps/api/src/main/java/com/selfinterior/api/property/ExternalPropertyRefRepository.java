package com.selfinterior.api.property;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalPropertyRefRepository
    extends JpaRepository<ExternalPropertyRefEntity, UUID> {
  void deleteByPropertyId(UUID propertyId);

  List<ExternalPropertyRefEntity> findByPropertyId(UUID propertyId);
}
