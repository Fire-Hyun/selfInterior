package com.selfinterior.api.expert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertCategoryRepository extends JpaRepository<ExpertCategoryEntity, UUID> {
  List<ExpertCategoryEntity> findByActiveTrueOrderByNameAsc();

  Optional<ExpertCategoryEntity> findByKeyAndActiveTrue(String key);
}
