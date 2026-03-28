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
public class MockOfficialFloorPlanClient implements OfficialFloorPlanClient {
  @Override
  public List<FloorPlanProviderCandidate> fetch(ProjectEntity project, PropertyEntity property) {
    if (property.getApartmentName() == null || !property.getApartmentName().contains("리센츠")) {
      return List.of();
    }
    return List.of(
        new FloorPlanProviderCandidate(
            "OFFICIAL_PLAN",
            LicenseStatus.APPROVED,
            AccessScope.COMMERCIAL_ALLOWED,
            "official-doc-rs84a",
            "RS84A",
            FloorPlanSourceType.OFFICIAL,
            "OFFICIAL_APPROVED_PLAN",
            MatchType.EXACT,
            93.2,
            ConfidenceGrade.EXACT,
            84.99,
            112.30,
            3,
            2,
            "84A",
            Map.of("provider", "OFFICIAL_PLAN", "layout", "84A"),
            List.of("거실 폭 실측"),
            Map.of(
                "planId", "normalized-rs84a",
                "sourceType", "OFFICIAL",
                "confidenceGrade", "EXACT",
                "spaces", List.of(Map.of("type", "LIVING_ROOM", "name", "거실")),
                "measurements", Map.of("requiredManualChecks", List.of("거실 폭 실측")))));
  }
}
