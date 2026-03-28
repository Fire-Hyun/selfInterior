package com.selfinterior.api.process;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessChecklistItemRepository
    extends JpaRepository<ProcessChecklistItemEntity, UUID> {
  List<ProcessChecklistItemEntity> findByProcessCatalogIdOrderByItemOrderAsc(UUID processCatalogId);
}
