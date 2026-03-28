package com.selfinterior.api.floorplan;

import com.selfinterior.api.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/floor-plans")
@RequiredArgsConstructor
public class FloorPlanController {
  private final FloorPlanResolutionService floorPlanResolutionService;

  @PostMapping("/resolve")
  public ApiResponse<FloorPlanResolveResponse> resolve(@PathVariable String projectId) {
    return ApiResponse.ok(floorPlanResolutionService.resolve(projectId));
  }

  @GetMapping
  public ApiResponse<FloorPlanListResponse> list(@PathVariable String projectId) {
    return ApiResponse.ok(floorPlanResolutionService.list(projectId));
  }

  @PostMapping("/{candidateId}/select")
  public ApiResponse<FloorPlanSelectResponse> select(
      @PathVariable String projectId,
      @PathVariable String candidateId,
      @Valid @RequestBody FloorPlanSelectRequest request) {
    return ApiResponse.ok(floorPlanResolutionService.select(projectId, candidateId, request));
  }

  public record FloorPlanResolveResponse(String resolutionStatus, int candidateCount) {}

  public record FloorPlanListResponse(
      String selectedPlanId, List<FloorPlanCandidateResponse> candidates) {}

  public record FloorPlanCandidateResponse(
      String id,
      String sourceType,
      String matchType,
      double confidenceScore,
      String confidenceGrade,
      String layoutLabel,
      Double exclusiveAreaM2,
      String licenseStatus,
      String source,
      String rawPayloadRef,
      String normalizedPlanRef,
      List<String> manualCheckItems) {}

  public record FloorPlanSelectRequest(@NotBlank String reason) {}

  public record FloorPlanSelectResponse(String selectedPlanId) {}
}
