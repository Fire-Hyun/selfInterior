package com.selfinterior.api.expert;

import com.selfinterior.api.common.api.ApiException;
import com.selfinterior.api.common.api.ErrorCode;
import com.selfinterior.api.expert.ExpertController.CreateExpertLeadRequest;
import com.selfinterior.api.expert.ExpertController.CreateExpertLeadResponse;
import com.selfinterior.api.expert.ExpertController.ExpertCategoryResponse;
import com.selfinterior.api.expert.ExpertController.ExpertDetailResponse;
import com.selfinterior.api.expert.ExpertController.ExpertRecommendationResponse;
import com.selfinterior.api.expert.ExpertController.ExpertResponse;
import com.selfinterior.api.project.ProjectEntity;
import com.selfinterior.api.project.ProjectRepository;
import com.selfinterior.api.property.PropertyEntity;
import com.selfinterior.api.property.PropertyRepository;
import com.selfinterior.api.visualqa.RiskLevel;
import com.selfinterior.api.visualqa.VisualAnswerEntity;
import com.selfinterior.api.visualqa.VisualAnswerRepository;
import com.selfinterior.api.visualqa.VisualQuestionEntity;
import com.selfinterior.api.visualqa.VisualQuestionImageEntity;
import com.selfinterior.api.visualqa.VisualQuestionImageRepository;
import com.selfinterior.api.visualqa.VisualQuestionRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpertLeadService {
  private final ExpertCategoryRepository expertCategoryRepository;
  private final ExpertRepository expertRepository;
  private final ExpertCategoryLinkRepository expertCategoryLinkRepository;
  private final ExpertServiceRegionRepository expertServiceRegionRepository;
  private final ExpertPortfolioRepository expertPortfolioRepository;
  private final ExpertLeadRepository expertLeadRepository;
  private final ExpertLeadEventRepository expertLeadEventRepository;
  private final ProjectRepository projectRepository;
  private final PropertyRepository propertyRepository;
  private final VisualQuestionRepository visualQuestionRepository;
  private final VisualAnswerRepository visualAnswerRepository;
  private final VisualQuestionImageRepository visualQuestionImageRepository;

  @Value("${app.default-owner-user-id}")
  private UUID defaultOwnerUserId;

  public List<ExpertCategoryResponse> listCategories() {
    return expertCategoryRepository.findByActiveTrueOrderByNameAsc().stream()
        .map(ExpertMapper::toCategory)
        .toList();
  }

  public List<ExpertResponse> listExperts(
      String categoryKey,
      String sido,
      String sigungu,
      Boolean partialSupported,
      Integer budgetMin,
      Integer budgetMax) {
    return loadActiveProfiles().stream()
        .filter(profile -> matchesCategory(profile, categoryKey))
        .filter(profile -> matchesRegion(profile, sido, sigungu))
        .filter(profile -> matchesBudget(profile.expert(), budgetMin, budgetMax))
        .filter(
            profile ->
                partialSupported == null
                    || !partialSupported
                    || profile.expert().isPartialWorkSupported())
        .map(profile -> toResponse(profile, null, null))
        .toList();
  }

  public ExpertDetailResponse getExpert(String expertId) {
    UUID id = UUID.fromString(expertId);
    ExpertProfile profile = findActiveProfile(id);
    List<String> leadStatuses =
        expertLeadRepository.findAll().stream()
            .filter(lead -> lead.getExpertId().equals(id))
            .map(lead -> lead.getLeadStatus().name())
            .distinct()
            .toList();
    return ExpertMapper.toDetail(toResponse(profile, null, null), leadStatuses);
  }

  public ExpertRecommendationResponse getRecommendations(String projectId) {
    RecommendationContext context = buildRecommendationContext(projectId);
    return buildRecommendation(context);
  }

  @Transactional
  public CreateExpertLeadResponse createLead(String projectId, CreateExpertLeadRequest request) {
    RecommendationContext context = buildRecommendationContext(projectId);
    UUID expertId = UUID.fromString(request.expertId());
    ExpertProfile expertProfile = findActiveProfile(expertId);
    ExpertCategoryEntity requestedCategory = findCategoryByKey(request.requestedCategoryKey());

    if (!matchesCategory(expertProfile, requestedCategory.getKey())) {
      throw new ApiException(
          ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "선택한 전문가는 요청 카테고리를 지원하지 않습니다.");
    }

    ExpertLeadEntity lead = new ExpertLeadEntity();
    lead.setProjectId(context.project().getId());
    lead.setExpertId(expertId);
    lead.setRequestedCategoryId(requestedCategory.getId());
    lead.setLeadStatus(LeadStatus.NEW);
    lead.setBudgetMin(
        request.budgetMin() != null ? request.budgetMin() : context.project().getBudgetMin());
    lead.setBudgetMax(
        request.budgetMax() != null ? request.budgetMax() : context.project().getBudgetMax());
    lead.setDesiredStartDate(request.desiredStartDate());
    lead.setMessage(request.message());
    lead.setCreatedByUserId(
        context.project().getOwnerUserId() != null
            ? context.project().getOwnerUserId()
            : defaultOwnerUserId);
    lead.setAttachmentPayload(
        buildAttachmentPayload(context, expertProfile, requestedCategory, request));
    ExpertLeadEntity savedLead = expertLeadRepository.save(lead);

    ExpertLeadEventEntity event = new ExpertLeadEventEntity();
    event.setExpertLeadId(savedLead.getId());
    event.setEventType(LeadEventType.CREATED);
    event.setPayload(
        Map.of(
            "expertId", request.expertId(),
            "requestedCategoryKey", request.requestedCategoryKey(),
            "message", request.message()));
    expertLeadEventRepository.save(event);

    return ExpertMapper.toLeadResponse(savedLead);
  }

  public ProjectExpertCard loadProjectHomeCard(String projectId) {
    ExpertRecommendationResponse response = getRecommendations(projectId);
    if (response.experts().isEmpty()) {
      return null;
    }

    ExpertResponse expert = response.experts().get(0);
    return new ProjectExpertCard(
        expert.companyName(),
        response.primaryCategoryName() != null
            ? response.primaryCategoryName()
            : response.primaryCategoryKey(),
        expert.recommendationReason(),
        expert.id());
  }

  private ExpertRecommendationResponse buildRecommendation(RecommendationContext context) {
    List<ScoredExpert> scoredExperts =
        loadActiveProfiles().stream()
            .filter(
                profile ->
                    matchesBudget(
                        profile.expert(),
                        context.project().getBudgetMin(),
                        context.project().getBudgetMax()))
            .map(profile -> scoreExpert(profile, context))
            .filter(scored -> scored.score() > 0)
            .sorted(Comparator.comparingDouble(ScoredExpert::score).reversed())
            .limit(5)
            .toList();

    return ExpertMapper.toRecommendation(
        context.primaryCategory() == null ? null : context.primaryCategory().getKey(),
        context.primaryCategory() == null ? null : context.primaryCategory().getName(),
        context.secondaryCategory() == null ? null : context.secondaryCategory().getKey(),
        context.secondaryCategory() == null ? null : context.secondaryCategory().getName(),
        buildRationale(context),
        scoredExperts.stream()
            .map(scored -> toResponse(scored.profile(), scored.score(), scored.reason()))
            .toList());
  }

  private RecommendationContext buildRecommendationContext(String projectId) {
    ProjectEntity project = findProject(projectId);
    PropertyEntity property = propertyRepository.findByProjectId(project.getId()).orElse(null);
    VisualQuestionEntity latestQuestion = loadLatestQuestion(project.getId());
    VisualAnswerEntity latestAnswer =
        latestQuestion == null
            ? null
            : visualAnswerRepository.findByQuestionId(latestQuestion.getId()).orElse(null);

    ExpertCategoryEntity primary =
        detectPrimaryCategory(project, latestQuestion, latestAnswer).orElse(null);
    ExpertCategoryEntity secondary = detectSecondaryCategory(project, primary).orElse(null);
    return new RecommendationContext(
        project, property, latestQuestion, latestAnswer, primary, secondary);
  }

  private Optional<ExpertCategoryEntity> detectPrimaryCategory(
      ProjectEntity project, VisualQuestionEntity latestQuestion, VisualAnswerEntity latestAnswer) {
    if (latestQuestion != null && latestAnswer != null && isHighRisk(latestAnswer)) {
      String combined =
          (latestQuestion.getQuestionText() + " " + latestAnswer.getObservedText())
              .toLowerCase(Locale.ROOT);
      if (containsAny(combined, "전기", "스위치", "콘센트", "차단기")) {
        return expertCategoryRepository.findByKeyAndActiveTrue("ELECTRICAL");
      }
      if (containsAny(combined, "누수", "결로", "곰팡이", "배관")) {
        return expertCategoryRepository.findByKeyAndActiveTrue("WATERPROOFING");
      }
      if (containsAny(combined, "타일", "욕실", "실리콘")) {
        return expertCategoryRepository.findByKeyAndActiveTrue("TILE_BATH");
      }
      if (containsAny(combined, "도배", "필름", "페인트", "마감")) {
        return expertCategoryRepository.findByKeyAndActiveTrue("SURFACE_FINISH");
      }
    }
    return categoryByProcessStep(project.getCurrentProcessStep());
  }

  private Optional<ExpertCategoryEntity> detectSecondaryCategory(
      ProjectEntity project, ExpertCategoryEntity primary) {
    Optional<ExpertCategoryEntity> byProcess =
        categoryByProcessStep(project.getCurrentProcessStep());
    if (byProcess.isPresent()
        && (primary == null || !byProcess.get().getKey().equals(primary.getKey()))) {
      return byProcess;
    }
    if ("ISSUE_DIAGNOSIS".equals(project.getCurrentProcessStep())) {
      return expertCategoryRepository.findByKeyAndActiveTrue("TILE_BATH");
    }
    if ("ELECTRICAL".equals(project.getCurrentProcessStep())) {
      return expertCategoryRepository.findByKeyAndActiveTrue("SURFACE_FINISH");
    }
    return Optional.empty();
  }

  private Optional<ExpertCategoryEntity> categoryByProcessStep(String currentProcessStep) {
    if (currentProcessStep == null || currentProcessStep.isBlank()) {
      return Optional.empty();
    }
    return switch (currentProcessStep) {
      case "ELECTRICAL" -> expertCategoryRepository.findByKeyAndActiveTrue("ELECTRICAL");
      case "SURFACE_FINISH", "FINAL_FIXTURE" ->
          expertCategoryRepository.findByKeyAndActiveTrue("SURFACE_FINISH");
      case "DEMOLITION" -> expertCategoryRepository.findByKeyAndActiveTrue("DEMOLITION");
      case "ISSUE_DIAGNOSIS" -> expertCategoryRepository.findByKeyAndActiveTrue("WATERPROOFING");
      default -> Optional.empty();
    };
  }

  private ScoredExpert scoreExpert(ExpertProfile profile, RecommendationContext context) {
    double score = 0;
    List<String> reasons = new ArrayList<>();

    if (context.primaryCategory() != null
        && matchesCategory(profile, context.primaryCategory().getKey())) {
      score += 40;
      reasons.add(context.primaryCategory().getName() + " 우선 카테고리 일치");
    }
    if (context.secondaryCategory() != null
        && matchesCategory(profile, context.secondaryCategory().getKey())) {
      score += 15;
      reasons.add(context.secondaryCategory().getName() + " 보조 카테고리 지원");
    }
    if (context.property() != null) {
      if (matchesExactRegion(profile, context.property())) {
        score += 20;
        reasons.add("서비스 지역이 현재 집 주소와 일치");
      } else if (matchesSido(profile, context.property().getSido())) {
        score += 10;
        reasons.add("같은 시도 서비스 가능");
      }
    }
    if (matchesBudget(
        profile.expert(), context.project().getBudgetMin(), context.project().getBudgetMax())) {
      score += 10;
      reasons.add("예산 범위와 겹침");
    }
    if ("PARTIAL".equals(context.project().getProjectType().name())
        && profile.expert().isPartialWorkSupported()) {
      score += 5;
      reasons.add("부분 시공 지원");
    }

    score += scale(profile.expert().getResponseScore(), 10);
    score += scale(profile.expert().getReviewScore(), 5);
    return new ScoredExpert(profile, score, String.join(" · ", reasons));
  }

  private Map<String, Object> buildAttachmentPayload(
      RecommendationContext context,
      ExpertProfile expertProfile,
      ExpertCategoryEntity requestedCategory,
      CreateExpertLeadRequest request) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put(
        "project",
        Map.of(
            "title", context.project().getTitle(),
            "budgetMin", valueOrBlank(context.project().getBudgetMin()),
            "budgetMax", valueOrBlank(context.project().getBudgetMax()),
            "currentProcessStep", blankToEmpty(context.project().getCurrentProcessStep())));
    payload.put(
        "property",
        context.property() == null
            ? Map.of()
            : Map.of(
                "roadAddress", blankToEmpty(context.property().getRoadAddress()),
                "sido", blankToEmpty(context.property().getSido()),
                "sigungu", blankToEmpty(context.property().getSigungu()),
                "apartmentName", blankToEmpty(context.property().getApartmentName()),
                "exclusiveAreaM2",
                    context.property().getExclusiveAreaM2() == null
                        ? ""
                        : context.property().getExclusiveAreaM2().toPlainString()));
    payload.put(
        "request",
        Map.of(
            "expertCompanyName", expertProfile.expert().getCompanyName(),
            "requestedCategoryKey", requestedCategory.getKey(),
            "requestedCategoryName", requestedCategory.getName(),
            "message", request.message(),
            "desiredStartDate",
                request.desiredStartDate() == null ? "" : request.desiredStartDate().toString()));
    payload.put(
        "latestQuestion", latestQuestionPayload(context.latestQuestion(), context.latestAnswer()));
    return payload;
  }

  private Map<String, Object> latestQuestionPayload(
      VisualQuestionEntity latestQuestion, VisualAnswerEntity latestAnswer) {
    if (latestQuestion == null) {
      return Map.of();
    }
    List<VisualQuestionImageEntity> images =
        visualQuestionImageRepository.findByQuestionIdOrderByCreatedAtAsc(latestQuestion.getId());
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("questionText", latestQuestion.getQuestionText());
    payload.put("processStepKey", blankToEmpty(latestQuestion.getProcessStepKey()));
    payload.put("spaceType", latestQuestion.getSpaceType().name());
    payload.put(
        "imagePaths", images.stream().map(VisualQuestionImageEntity::getStoragePath).toList());
    if (latestAnswer != null) {
      payload.put("riskLevel", latestAnswer.getRiskLevel().name());
      payload.put("observedText", latestAnswer.getObservedText());
      payload.put("expertRequired", latestAnswer.isExpertRequired());
    }
    return payload;
  }

  private ProjectEntity findProject(String projectId) {
    return projectRepository
        .findById(UUID.fromString(projectId))
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
  }

  private ExpertCategoryEntity findCategoryByKey(String key) {
    return expertCategoryRepository
        .findByKeyAndActiveTrue(key)
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.EXPERT_CATEGORY_NOT_FOUND,
                    HttpStatus.NOT_FOUND,
                    "전문가 카테고리를 찾을 수 없습니다."));
  }

  private VisualQuestionEntity loadLatestQuestion(UUID projectId) {
    List<VisualQuestionEntity> questions =
        visualQuestionRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    return questions.isEmpty() ? null : questions.get(0);
  }

  private List<ExpertProfile> loadActiveProfiles() {
    List<ExpertEntity> experts = expertRepository.findByStatus(ExpertStatus.ACTIVE);
    List<UUID> expertIds = experts.stream().map(ExpertEntity::getId).toList();
    Map<UUID, List<ExpertCategoryEntity>> categories = loadCategories(expertIds);
    Map<UUID, List<ExpertServiceRegionEntity>> regions = loadRegions(expertIds);
    Map<UUID, List<ExpertPortfolioEntity>> portfolios = loadPortfolios(expertIds);
    return experts.stream()
        .map(
            expert ->
                new ExpertProfile(
                    expert,
                    categories.getOrDefault(expert.getId(), List.of()),
                    regions.getOrDefault(expert.getId(), List.of()),
                    portfolios.getOrDefault(expert.getId(), List.of())))
        .toList();
  }

  private ExpertProfile findActiveProfile(UUID expertId) {
    return loadActiveProfiles().stream()
        .filter(profile -> profile.expert().getId().equals(expertId))
        .findFirst()
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.EXPERT_NOT_FOUND, HttpStatus.NOT_FOUND, "전문가를 찾을 수 없습니다."));
  }

  private Map<UUID, List<ExpertCategoryEntity>> loadCategories(List<UUID> expertIds) {
    Map<UUID, ExpertCategoryEntity> categoriesById = new HashMap<>();
    for (ExpertCategoryEntity category : expertCategoryRepository.findAll()) {
      categoriesById.put(category.getId(), category);
    }
    Map<UUID, List<ExpertCategoryEntity>> result = new HashMap<>();
    for (ExpertCategoryLinkEntity link : expertCategoryLinkRepository.findByExpertIdIn(expertIds)) {
      ExpertCategoryEntity category = categoriesById.get(link.getExpertCategoryId());
      if (category == null || !category.isActive()) {
        continue;
      }
      result.computeIfAbsent(link.getExpertId(), ignored -> new ArrayList<>()).add(category);
    }
    return result;
  }

  private Map<UUID, List<ExpertServiceRegionEntity>> loadRegions(List<UUID> expertIds) {
    Map<UUID, List<ExpertServiceRegionEntity>> result = new HashMap<>();
    for (ExpertServiceRegionEntity region :
        expertServiceRegionRepository.findByExpertIdIn(expertIds)) {
      result.computeIfAbsent(region.getExpertId(), ignored -> new ArrayList<>()).add(region);
    }
    return result;
  }

  private Map<UUID, List<ExpertPortfolioEntity>> loadPortfolios(List<UUID> expertIds) {
    Map<UUID, List<ExpertPortfolioEntity>> result = new HashMap<>();
    for (UUID expertId : expertIds) {
      result.put(expertId, expertPortfolioRepository.findByExpertId(expertId));
    }
    return result;
  }

  private ExpertResponse toResponse(
      ExpertProfile profile, Double recommendationScore, String recommendationReason) {
    return ExpertMapper.toExpertResponse(
        profile.expert(),
        profile.categories(),
        profile.regions(),
        profile.portfolios(),
        recommendationScore,
        recommendationReason);
  }

  private boolean matchesCategory(ExpertProfile profile, String categoryKey) {
    if (categoryKey == null || categoryKey.isBlank()) {
      return true;
    }
    return profile.categories().stream()
        .anyMatch(category -> category.getKey().equals(categoryKey));
  }

  private boolean matchesRegion(ExpertProfile profile, String sido, String sigungu) {
    if ((sido == null || sido.isBlank()) && (sigungu == null || sigungu.isBlank())) {
      return true;
    }
    return profile.regions().stream()
        .anyMatch(
            region ->
                (sido == null || sido.isBlank() || region.getSido().equals(sido))
                    && (sigungu == null
                        || sigungu.isBlank()
                        || sigungu.equals(region.getSigungu())
                        || region.getSigungu() == null));
  }

  private boolean matchesBudget(ExpertEntity expert, Integer budgetMin, Integer budgetMax) {
    if (budgetMin == null && budgetMax == null) {
      return true;
    }
    int queryMin = budgetMin == null ? Integer.MIN_VALUE : budgetMin;
    int queryMax = budgetMax == null ? Integer.MAX_VALUE : budgetMax;
    int expertMin = expert.getMinBudget() == null ? Integer.MIN_VALUE : expert.getMinBudget();
    int expertMax = expert.getMaxBudget() == null ? Integer.MAX_VALUE : expert.getMaxBudget();
    return queryMin <= expertMax && expertMin <= queryMax;
  }

  private boolean matchesExactRegion(ExpertProfile profile, PropertyEntity property) {
    return property.getSido() != null
        && property.getSigungu() != null
        && profile.regions().stream()
            .anyMatch(
                region ->
                    property.getSido().equals(region.getSido())
                        && property.getSigungu().equals(region.getSigungu()));
  }

  private boolean matchesSido(ExpertProfile profile, String sido) {
    return sido != null
        && profile.regions().stream().anyMatch(region -> sido.equals(region.getSido()));
  }

  private boolean isHighRisk(VisualAnswerEntity latestAnswer) {
    return latestAnswer.getRiskLevel() == RiskLevel.HIGH
        || latestAnswer.getRiskLevel() == RiskLevel.CRITICAL;
  }

  private boolean containsAny(String source, String... keywords) {
    for (String keyword : keywords) {
      if (source.contains(keyword)) {
        return true;
      }
    }
    return false;
  }

  private double scale(BigDecimal value, double max) {
    if (value == null) {
      return 0;
    }
    return Math.min(max, value.doubleValue() / 10.0);
  }

  private String buildRationale(RecommendationContext context) {
    List<String> parts = new ArrayList<>();
    if (context.primaryCategory() != null) {
      parts.add("우선 카테고리 " + context.primaryCategory().getName());
    }
    if (context.property() != null && context.property().getSigungu() != null) {
      parts.add(context.property().getSigungu() + " 서비스 가능 업체 우선");
    }
    if (context.project().getBudgetMin() != null || context.project().getBudgetMax() != null) {
      parts.add("예산 범위 고려");
    }
    if (context.latestAnswer() != null && isHighRisk(context.latestAnswer())) {
      parts.add("최근 질문 위험도 반영");
    }
    return String.join(" · ", parts);
  }

  private Object valueOrBlank(Integer value) {
    return value == null ? "" : value;
  }

  private String blankToEmpty(String value) {
    return value == null ? "" : value;
  }

  private record ExpertProfile(
      ExpertEntity expert,
      List<ExpertCategoryEntity> categories,
      List<ExpertServiceRegionEntity> regions,
      List<ExpertPortfolioEntity> portfolios) {}

  private record ScoredExpert(ExpertProfile profile, double score, String reason) {}

  private record RecommendationContext(
      ProjectEntity project,
      PropertyEntity property,
      VisualQuestionEntity latestQuestion,
      VisualAnswerEntity latestAnswer,
      ExpertCategoryEntity primaryCategory,
      ExpertCategoryEntity secondaryCategory) {}

  public record ProjectExpertCard(
      String companyName, String categoryName, String recommendationReason, String expertId) {}
}
