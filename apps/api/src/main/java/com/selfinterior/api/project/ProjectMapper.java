package com.selfinterior.api.project;

import com.selfinterior.api.floorplan.FloorPlanCandidateEntity;
import com.selfinterior.api.floorplan.FloorPlanSourceEntity;
import com.selfinterior.api.project.ProjectController.HomeFloorPlanCardResponse;
import com.selfinterior.api.project.ProjectController.HomeProjectSummaryResponse;
import com.selfinterior.api.project.ProjectController.HomePropertyCardResponse;
import com.selfinterior.api.project.ProjectController.ProjectDetailResponse;
import com.selfinterior.api.project.ProjectController.ProjectSummaryResponse;
import com.selfinterior.api.project.ProjectController.PropertySnapshotResponse;
import com.selfinterior.api.project.ProjectController.SelectedFloorPlanResponse;
import com.selfinterior.api.property.PropertyEntity;
import java.math.BigDecimal;
import java.util.List;

public final class ProjectMapper {
  private ProjectMapper() {}

  public static ProjectSummaryResponse toSummary(ProjectEntity entity, boolean propertyAttached) {
    return new ProjectSummaryResponse(
        entity.getId().toString(),
        entity.getTitle(),
        entity.getProjectType().name(),
        entity.getLivingStatus().name(),
        entity.getBudgetMin(),
        entity.getBudgetMax(),
        propertyAttached);
  }

  public static ProjectDetailResponse toDetail(
      ProjectEntity entity,
      PropertyEntity property,
      FloorPlanCandidateEntity selectedCandidate,
      FloorPlanSourceEntity selectedSource,
      int floorPlanCandidateCount) {
    return new ProjectDetailResponse(
        entity.getId().toString(),
        entity.getTitle(),
        entity.getProjectType().name(),
        entity.getLivingStatus().name(),
        entity.getBudgetMin(),
        entity.getBudgetMax(),
        property != null,
        property == null
            ? null
            : new PropertySnapshotResponse(
                property.getApartmentName(),
                property.getRoadAddress(),
                property.getDongNo(),
                property.getHoNo(),
                property.getExclusiveAreaM2() == null
                    ? null
                    : property.getExclusiveAreaM2().doubleValue(),
                property.getRoomCount(),
                property.getBathroomCount()),
        selectedCandidate == null || selectedSource == null
            ? null
            : new SelectedFloorPlanResponse(
                selectedCandidate.getId().toString(),
                selectedCandidate.getLayoutLabel(),
                selectedCandidate.getConfidenceGrade().name(),
                selectedCandidate.getConfidenceScore().doubleValue(),
                selectedCandidate.getSourceType().name(),
                selectedSource.getLicenseStatus().name(),
                selectedCandidate.getSource()),
        floorPlanCandidateCount);
  }

  public static HomeProjectSummaryResponse toHomeProject(ProjectEntity entity) {
    return new HomeProjectSummaryResponse(
        entity.getId().toString(),
        entity.getTitle(),
        entity.getProjectType().name(),
        entity.getLivingStatus().name(),
        entity.getCurrentProcessStep(),
        entity.isOnboardingCompleted());
  }

  public static HomePropertyCardResponse toHomeProperty(PropertyEntity property) {
    if (property == null) {
      return null;
    }

    return new HomePropertyCardResponse(
        property.getApartmentName(),
        property.getRoadAddress(),
        property.getDongNo(),
        property.getHoNo(),
        property.getCompletionYear(),
        property.getHouseholdCount(),
        property.getExclusiveAreaM2() == null ? null : property.getExclusiveAreaM2().doubleValue());
  }

  public static HomeFloorPlanCardResponse toHomeFloorPlan(
      FloorPlanCandidateEntity selectedCandidate,
      FloorPlanSourceEntity selectedSource,
      int candidateCount,
      List<String> manualCheckItems) {
    if (selectedCandidate == null || selectedSource == null) {
      return null;
    }

    return new HomeFloorPlanCardResponse(
        selectedCandidate.getId().toString(),
        selectedCandidate.getLayoutLabel(),
        selectedCandidate.getConfidenceGrade().name(),
        selectedCandidate.getConfidenceScore().doubleValue(),
        selectedCandidate.getSourceType().name(),
        selectedCandidate.getSource(),
        selectedSource.getLicenseStatus().name(),
        candidateCount,
        buildStructureSummary(selectedCandidate),
        manualCheckItems);
  }

  private static String buildStructureSummary(FloorPlanCandidateEntity candidate) {
    String areaText = formatArea(candidate.getExclusiveAreaM2());
    Integer roomCount = candidate.getRoomCount();
    Integer bathroomCount = candidate.getBathroomCount();

    return String.format(
        "전용 %s㎡ · 방 %d개 · 욕실 %d개",
        areaText == null ? "-" : areaText,
        roomCount == null ? 0 : roomCount,
        bathroomCount == null ? 0 : bathroomCount);
  }

  private static String formatArea(BigDecimal area) {
    if (area == null) {
      return null;
    }
    return area.stripTrailingZeros().toPlainString();
  }
}
