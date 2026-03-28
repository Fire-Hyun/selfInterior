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
public class MockApproximateFloorPlanGenerator implements ApproximateFloorPlanGenerator {
  @Override
  public FloorPlanProviderCandidate generate(ProjectEntity project, PropertyEntity property) {
    return new FloorPlanProviderCandidate(
        "GENERATED",
        LicenseStatus.INTERNAL,
        AccessScope.INTERNAL_ONLY,
        "generated-" + project.getId(),
        "APPROX-" + project.getId(),
        FloorPlanSourceType.APPROX,
        "APPROX_GENERATOR",
        MatchType.APPROX_GENERATED,
        61.5,
        ConfidenceGrade.APPROX,
        property.getExclusiveAreaM2() == null ? 59.97 : property.getExclusiveAreaM2().doubleValue(),
        null,
        property.getRoomCount() == null ? 3 : property.getRoomCount(),
        property.getBathroomCount() == null ? 2 : property.getBathroomCount(),
        "근사 구조",
        Map.of("provider", "GENERATED", "projectId", project.getId().toString()),
        List.of("욕실 젖은 구역 배수구 위치 확인"),
        Map.of(
            "planId",
            "approx-" + project.getId(),
            "sourceType",
            "APPROX",
            "confidenceGrade",
            "APPROX",
            "spaces",
            List.of(Map.of("type", "BEDROOM", "name", "침실1"))));
  }
}
