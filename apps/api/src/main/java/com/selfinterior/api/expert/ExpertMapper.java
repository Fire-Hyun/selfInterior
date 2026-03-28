package com.selfinterior.api.expert;

import com.selfinterior.api.expert.ExpertController.CreateExpertLeadResponse;
import com.selfinterior.api.expert.ExpertController.ExpertCategoryResponse;
import com.selfinterior.api.expert.ExpertController.ExpertDetailResponse;
import com.selfinterior.api.expert.ExpertController.ExpertPortfolioResponse;
import com.selfinterior.api.expert.ExpertController.ExpertRecommendationResponse;
import com.selfinterior.api.expert.ExpertController.ExpertResponse;
import com.selfinterior.api.expert.ExpertController.ExpertServiceRegionResponse;
import java.math.BigDecimal;
import java.util.List;

public final class ExpertMapper {
  private ExpertMapper() {}

  public static ExpertCategoryResponse toCategory(ExpertCategoryEntity category) {
    return new ExpertCategoryResponse(
        category.getId().toString(), category.getKey(), category.getName());
  }

  public static ExpertResponse toExpertResponse(
      ExpertEntity expert,
      List<ExpertCategoryEntity> categories,
      List<ExpertServiceRegionEntity> regions,
      List<ExpertPortfolioEntity> portfolios,
      Double recommendationScore,
      String recommendationReason) {
    return new ExpertResponse(
        expert.getId().toString(),
        expert.getCompanyName(),
        expert.getContactName(),
        expert.getPhone(),
        expert.getEmail(),
        expert.getIntroText(),
        expert.getMinBudget(),
        expert.getMaxBudget(),
        expert.isPartialWorkSupported(),
        expert.isSemiSelfCollaborationSupported(),
        decimalToDouble(expert.getResponseScore()),
        decimalToDouble(expert.getReviewScore()),
        categories.stream().map(ExpertCategoryEntity::getKey).toList(),
        categories.stream().map(ExpertCategoryEntity::getName).toList(),
        regions.stream().map(ExpertMapper::toRegion).toList(),
        portfolios.stream().map(ExpertMapper::toPortfolio).toList(),
        recommendationScore,
        recommendationReason,
        expert.getLicenseInfo());
  }

  public static ExpertDetailResponse toDetail(ExpertResponse expert, List<String> leadStatuses) {
    return new ExpertDetailResponse(expert, leadStatuses);
  }

  public static ExpertRecommendationResponse toRecommendation(
      String primaryCategoryKey,
      String primaryCategoryName,
      String secondaryCategoryKey,
      String secondaryCategoryName,
      String rationale,
      List<ExpertResponse> experts) {
    return new ExpertRecommendationResponse(
        primaryCategoryKey,
        primaryCategoryName,
        secondaryCategoryKey,
        secondaryCategoryName,
        rationale,
        experts);
  }

  public static CreateExpertLeadResponse toLeadResponse(ExpertLeadEntity lead) {
    return new CreateExpertLeadResponse(lead.getId().toString(), lead.getLeadStatus().name());
  }

  private static ExpertServiceRegionResponse toRegion(ExpertServiceRegionEntity region) {
    return new ExpertServiceRegionResponse(region.getSido(), region.getSigungu());
  }

  private static ExpertPortfolioResponse toPortfolio(ExpertPortfolioEntity portfolio) {
    return new ExpertPortfolioResponse(
        portfolio.getId().toString(),
        portfolio.getTitle(),
        portfolio.getDescription(),
        portfolio.getStorageKey(),
        portfolio.getMetadata());
  }

  private static Double decimalToDouble(BigDecimal value) {
    return value == null ? null : value.doubleValue();
  }
}
