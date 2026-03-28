package com.selfinterior.api.process;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessGuideRepository extends JpaRepository<ProcessGuideEntity, UUID> {
  Optional<ProcessGuideEntity> findByProcessCatalogId(UUID processCatalogId);
}
