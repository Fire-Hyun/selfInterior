package com.selfinterior.api.floorplan;

import com.selfinterior.api.floorplan.FloorPlanController.FloorPlanCandidateResponse;
import java.util.List;

public final class FloorPlanMapper {
  private FloorPlanMapper() {}

  public static FloorPlanCandidateResponse toResponse(
      FloorPlanCandidateEntity candidate,
      FloorPlanSourceEntity source,
      List<String> manualCheckItems) {
    return new FloorPlanCandidateResponse(
        candidate.getId().toString(),
        candidate.getSourceType().name(),
        candidate.getMatchType().name(),
        candidate.getConfidenceScore().doubleValue(),
        candidate.getConfidenceGrade().name(),
        candidate.getLayoutLabel(),
        candidate.getExclusiveAreaM2() == null
            ? null
            : candidate.getExclusiveAreaM2().doubleValue(),
        source.getLicenseStatus().name(),
        candidate.getSource(),
        candidate.getRawPayloadRef(),
        candidate.getNormalizedPlanRef(),
        manualCheckItems);
  }
}
