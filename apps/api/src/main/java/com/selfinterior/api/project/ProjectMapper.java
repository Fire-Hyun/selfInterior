package com.selfinterior.api.project;

import com.selfinterior.api.floorplan.FloorPlanCandidateEntity;
import com.selfinterior.api.floorplan.FloorPlanSourceEntity;
import com.selfinterior.api.project.ProjectController.ProjectDetailResponse;
import com.selfinterior.api.project.ProjectController.ProjectSummaryResponse;
import com.selfinterior.api.project.ProjectController.PropertySnapshotResponse;
import com.selfinterior.api.project.ProjectController.SelectedFloorPlanResponse;
import com.selfinterior.api.property.PropertyEntity;

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
}
