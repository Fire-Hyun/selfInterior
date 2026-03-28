package com.selfinterior.api.process;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessCatalogRepository extends JpaRepository<ProcessCatalogEntity, UUID> {
  List<ProcessCatalogEntity> findAllByOrderBySortOrderAsc();

  Optional<ProcessCatalogEntity> findByStepKey(String stepKey);
}
