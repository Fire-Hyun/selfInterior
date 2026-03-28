package com.selfinterior.api.floorplan.provider;

import com.selfinterior.api.floorplan.AccessScope;
import com.selfinterior.api.floorplan.ConfidenceGrade;
import com.selfinterior.api.floorplan.FloorPlanProviderCandidate;
import com.selfinterior.api.floorplan.FloorPlanSourceType;
import com.selfinterior.api.floorplan.LicenseStatus;
import com.selfinterior.api.floorplan.MatchType;
import com.selfinterior.api.project.ProjectEntity;
import com.selfinterior.api.property.PropertyEntity;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockLicensedFloorPlanClient implements LicensedFloorPlanClient {
  @Override
  public List<FloorPlanProviderCandidate> fetch(ProjectEntity project, PropertyEntity property) {
    if (property.getApartmentName() == null) {
      return List.of();
    }
    return List.of(
        new FloorPlanProviderCandidate(
            "LICENSED_PLAN",
            LicenseStatus.RESTRICTED,
            AccessScope.ORG_ONLY,
            "licensed-doc-default",
            "LIC-" + property.getApartmentName(),
            FloorPlanSourceType.LICENSED,
            "LICENSED_PROVIDER_PLAN",
            MatchType.SAME_COMPLEX_SIMILAR_AREA,
            78.4,
            ConfidenceGrade.HIGH,
            property.getExclusiveAreaM2() == null
                ? 84.99
                : property.getExclusiveAreaM2().doubleValue(),
            110.20,
            property.getRoomCount() == null ? 3 : property.getRoomCount(),
            property.getBathroomCount() == null ? 2 : property.getBathroomCount(),
            "유사 84형",
            Map.of("provider", "LICENSED_PLAN", "apartmentName", property.getApartmentName()),
            List.of("주방-다이닝 간 벽체 두께 확인"),
            Map.of(
                "planId", "licensed-normalized",
                "sourceType", "LICENSED",
                "confidenceGrade", "HIGH",
                "spaces", List.of(Map.of("type", "KITCHEN", "name", "주방")))));
  }
}
