package com.selfinterior.api.project;

import com.selfinterior.api.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
  private final ProjectService projectService;

  @PostMapping
  public ApiResponse<CreateProjectResponse> create(
      @Valid @RequestBody CreateProjectRequest request) {
    return ApiResponse.ok(projectService.create(request));
  }

  @GetMapping
  public ApiResponse<ProjectListResponse> list() {
    return ApiResponse.ok(new ProjectListResponse(projectService.list()));
  }

  @GetMapping("/{projectId}")
  public ApiResponse<ProjectDetailResponse> get(@PathVariable String projectId) {
    return ApiResponse.ok(projectService.get(projectId));
  }

  @PatchMapping("/{projectId}")
  public ApiResponse<ProjectDetailResponse> update(
      @PathVariable String projectId, @Valid @RequestBody UpdateProjectRequest request) {
    return ApiResponse.ok(projectService.update(projectId, request));
  }

  public record CreateProjectRequest(
      @NotBlank String title,
      @NotNull ProjectType projectType,
      @NotNull LivingStatus livingStatus,
      Integer budgetMin,
      Integer budgetMax) {}

  public record UpdateProjectRequest(String title, Integer budgetMin, Integer budgetMax) {}

  public record CreateProjectResponse(ProjectSummaryResponse project) {}

  public record ProjectSummaryResponse(
      String id,
      String title,
      String projectType,
      String livingStatus,
      Integer budgetMin,
      Integer budgetMax,
      boolean propertyAttached) {}

  public record ProjectDetailResponse(
      String id,
      String title,
      String projectType,
      String livingStatus,
      Integer budgetMin,
      Integer budgetMax,
      boolean propertyAttached,
      PropertySnapshotResponse property,
      SelectedFloorPlanResponse selectedFloorPlan,
      int floorPlanCandidateCount) {}

  public record PropertySnapshotResponse(
      String apartmentName,
      String roadAddress,
      String dongNo,
      String hoNo,
      Double exclusiveAreaM2,
      Integer roomCount,
      Integer bathroomCount) {}

  public record SelectedFloorPlanResponse(
      String candidateId,
      String layoutLabel,
      String confidenceGrade,
      double confidenceScore,
      String sourceType,
      String licenseStatus,
      String source) {}

  public record ProjectListResponse(List<ProjectSummaryResponse> projects) {}
}
