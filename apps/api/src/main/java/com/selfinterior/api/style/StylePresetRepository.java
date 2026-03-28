package com.selfinterior.api.style;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StylePresetRepository extends JpaRepository<StylePresetEntity, UUID> {
  List<StylePresetEntity> findByActiveTrueOrderByNameAsc();

  Optional<StylePresetEntity> findByKeyAndActiveTrue(String key);
}
