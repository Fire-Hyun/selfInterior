package com.selfinterior.api.expert;

import com.selfinterior.api.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class ExpertController {
  private final ExpertLeadService expertLeadService;

  @GetMapping("/api/v1/expert-categories")
  public ApiResponse<ExpertCategoryListResponse> listCategories() {
    return ApiResponse.ok(new ExpertCategoryListResponse(expertLeadService.listCategories()));
  }

  @GetMapping("/api/v1/experts")
  public ApiResponse<ExpertListResponse> listExperts(
      @RequestParam(required = false) String categoryKey,
      @RequestParam(required = false) String sido,
      @RequestParam(required = false) String sigungu,
      @RequestParam(required = false) Boolean partialSupported,
      @RequestParam(required = false) Integer budgetMin,
      @RequestParam(required = false) Integer budgetMax) {
    return ApiResponse.ok(
        new ExpertListResponse(
            expertLeadService.listExperts(
                categoryKey, sido, sigungu, partialSupported, budgetMin, budgetMax)));
  }

  @GetMapping("/api/v1/experts/{expertId}")
  public ApiResponse<ExpertDetailResponse> getExpert(@PathVariable String expertId) {
    return ApiResponse.ok(expertLeadService.getExpert(expertId));
  }

  @GetMapping("/api/v1/projects/{projectId}/expert-recommendations")
  public ApiResponse<ExpertRecommendationResponse> getRecommendations(
      @PathVariable String projectId) {
    return ApiResponse.ok(expertLeadService.getRecommendations(projectId));
  }

  @PostMapping("/api/v1/projects/{projectId}/expert-leads")
  public ApiResponse<CreateExpertLeadResponse> createLead(
      @PathVariable String projectId, @Valid @RequestBody CreateExpertLeadRequest request) {
    return ApiResponse.ok(expertLeadService.createLead(projectId, request));
  }

  public record ExpertCategoryListResponse(List<ExpertCategoryResponse> categories) {}

  public record ExpertCategoryResponse(String id, String key, String name) {}

  public record ExpertListResponse(List<ExpertResponse> experts) {}

  public record ExpertResponse(
      String id,
      String companyName,
      String contactName,
      String phone,
      String email,
      String introText,
      Integer minBudget,
      Integer maxBudget,
      boolean partialWorkSupported,
      boolean semiSelfCollaborationSupported,
      Double responseScore,
      Double reviewScore,
      List<String> categoryKeys,
      List<String> categoryNames,
      List<ExpertServiceRegionResponse> serviceRegions,
      List<ExpertPortfolioResponse> portfolios,
      Double recommendationScore,
      String recommendationReason,
      Map<String, Object> licenseInfo) {}

  public record ExpertServiceRegionResponse(String sido, String sigungu) {}

  public record ExpertPortfolioResponse(
      String id,
      String title,
      String description,
      String storageKey,
      Map<String, Object> metadata) {}

  public record ExpertDetailResponse(ExpertResponse expert, List<String> leadStatuses) {}

  public record ExpertRecommendationResponse(
      String primaryCategoryKey,
      String primaryCategoryName,
      String secondaryCategoryKey,
      String secondaryCategoryName,
      String rationale,
      List<ExpertResponse> experts) {}

  public record CreateExpertLeadRequest(
      @NotBlank String expertId,
      @NotBlank String requestedCategoryKey,
      Integer budgetMin,
      Integer budgetMax,
      LocalDate desiredStartDate,
      @NotBlank String message) {}

  public record CreateExpertLeadResponse(String leadId, String leadStatus) {}
}
