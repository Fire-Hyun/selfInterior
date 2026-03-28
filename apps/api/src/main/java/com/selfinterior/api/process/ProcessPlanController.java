package com.selfinterior.api.process;

import com.selfinterior.api.common.api.ApiResponse;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/process-plan")
@RequiredArgsConstructor
public class ProcessPlanController {
  private final ProcessPlanService processPlanService;

  @PostMapping("/generate")
  public ApiResponse<ProcessPlanResponse> generate(@PathVariable String projectId) {
    return ApiResponse.ok(processPlanService.generate(projectId));
  }

  @GetMapping
  public ApiResponse<ProcessPlanResponse> get(@PathVariable String projectId) {
    return ApiResponse.ok(processPlanService.get(projectId));
  }

  @GetMapping("/steps/{stepKey}")
  public ApiResponse<ProcessPlanStepDetailResponse> getStep(
      @PathVariable String projectId, @PathVariable String stepKey) {
    return ApiResponse.ok(processPlanService.getStep(projectId, stepKey));
  }

  @PatchMapping("/tasks/{taskId}")
  public ApiResponse<ProcessTaskToggleResponse> toggleTask(
      @PathVariable String projectId,
      @PathVariable String taskId,
      @Valid @RequestBody ProcessTaskToggleRequest request) {
    return ApiResponse.ok(processPlanService.toggleTask(projectId, taskId, request));
  }

  public record ProcessPlanResponse(
      String planId,
      String status,
      String currentStepKey,
      int progressPercent,
      OffsetDateTime generatedAt,
      List<ProcessStepSummaryResponse> steps) {}

  public record ProcessStepSummaryResponse(
      String stepKey,
      String title,
      String status,
      int sortOrder,
      int durationDays,
      boolean required,
      int completedTaskCount,
      int totalTaskCount) {}

  public record ProcessPlanStepDetailResponse(
      String planId,
      ProcessStepSummaryResponse step,
      String purposeText,
      String startCheckIntro,
      List<String> decisionPoints,
      String selfWorkText,
      String expertRequiredText,
      String mistakesText,
      List<String> nextStepChecks,
      List<ProcessTaskResponse> tasks) {}

  public record ProcessTaskResponse(
      String id,
      String taskGroup,
      int itemOrder,
      String title,
      String description,
      boolean completed) {}

  public record ProcessTaskToggleRequest(boolean completed) {}

  public record ProcessTaskToggleResponse(
      String taskId, boolean completed, String stepStatus, String currentStepKey) {}
}
