package com.selfinterior.api.floorplan;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FloorPlanSourceRepository extends JpaRepository<FloorPlanSourceEntity, UUID> {
  Optional<FloorPlanSourceEntity> findByProviderAndProviderDocRef(
      String provider, String providerDocRef);
}
