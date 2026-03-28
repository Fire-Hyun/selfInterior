package com.selfinterior.api.floorplan;

import com.selfinterior.api.common.api.ApiException;
import com.selfinterior.api.common.api.ErrorCode;
import com.selfinterior.api.floorplan.FloorPlanController.FloorPlanListResponse;
import com.selfinterior.api.floorplan.FloorPlanController.FloorPlanResolveResponse;
import com.selfinterior.api.floorplan.FloorPlanController.FloorPlanSelectRequest;
import com.selfinterior.api.floorplan.FloorPlanController.FloorPlanSelectResponse;
import com.selfinterior.api.floorplan.provider.ApproximateFloorPlanGenerator;
import com.selfinterior.api.floorplan.provider.LicensedFloorPlanClient;
import com.selfinterior.api.floorplan.provider.OfficialFloorPlanClient;
import com.selfinterior.api.integration.IntegrationLogService;
import com.selfinterior.api.project.ProjectEntity;
import com.selfinterior.api.project.ProjectRepository;
import com.selfinterior.api.property.PropertyEntity;
import com.selfinterior.api.property.PropertyRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FloorPlanResolutionService {
  private final ProjectRepository projectRepository;
  private final PropertyRepository propertyRepository;
  private final FloorPlanSourceRepository floorPlanSourceRepository;
  private final FloorPlanCandidateRepository floorPlanCandidateRepository;
  private final NormalizedFloorPlanRepository normalizedFloorPlanRepository;
  private final OfficialFloorPlanClient officialFloorPlanClient;
  private final LicensedFloorPlanClient licensedFloorPlanClient;
  private final ApproximateFloorPlanGenerator approximateFloorPlanGenerator;
  private final IntegrationLogService integrationLogService;

  @Transactional
  public FloorPlanResolveResponse resolve(String projectId) {
    UUID parsedProjectId = UUID.fromString(projectId);
    ProjectEntity project =
        projectRepository
            .findById(parsedProjectId)
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
    PropertyEntity property =
        propertyRepository
            .findByProjectId(parsedProjectId)
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.PROPERTY_NOT_FOUND, HttpStatus.NOT_FOUND, "집 정보가 연결되지 않았습니다."));

    List<FloorPlanProviderCandidate> candidates = new ArrayList<>();
    candidates.addAll(
        callProvider(
            "OFFICIAL_PLAN",
            "floor-plan-resolve",
            project,
            property,
            () -> officialFloorPlanClient.fetch(project, property)));
    if (candidates.isEmpty()) {
      candidates.addAll(
          callProvider(
              "LICENSED_PLAN",
              "floor-plan-resolve",
              project,
              property,
              () -> licensedFloorPlanClient.fetch(project, property)));
    }
    if (candidates.isEmpty()) {
      candidates.add(approximateFloorPlanGenerator.generate(project, property));
    }

    for (int i = 0; i < candidates.size(); i++) {
      FloorPlanProviderCandidate candidate = candidates.get(i);
      FloorPlanSourceEntity source = new FloorPlanSourceEntity();
      source.setProvider(candidate.provider());
      source.setLicenseStatus(candidate.licenseStatus());
      source.setAccessScope(candidate.accessScope());
      source.setProviderDocRef(candidate.providerDocRef());
      FloorPlanSourceEntity savedSource = floorPlanSourceRepository.save(source);

      FloorPlanCandidateEntity entity = new FloorPlanCandidateEntity();
      entity.setProjectId(project.getId());
      entity.setPropertyId(property.getId());
      entity.setFloorPlanSourceId(savedSource.getId());
      entity.setProviderPlanKey(candidate.providerPlanKey());
      entity.setSourceType(candidate.sourceType());
      entity.setSource(candidate.source());
      entity.setMatchType(candidate.matchType());
      entity.setConfidenceScore(BigDecimal.valueOf(candidate.confidenceScore()));
      entity.setConfidenceGrade(candidate.confidenceGrade());
      entity.setExclusiveAreaM2(
          candidate.exclusiveAreaM2() == null
              ? null
              : BigDecimal.valueOf(candidate.exclusiveAreaM2()));
      entity.setSupplyAreaM2(
          candidate.supplyAreaM2() == null ? null : BigDecimal.valueOf(candidate.supplyAreaM2()));
      entity.setRoomCount(candidate.roomCount());
      entity.setBathroomCount(candidate.bathroomCount());
      entity.setLayoutLabel(candidate.layoutLabel());
      entity.setSelected(i == 0);
      entity.setSelectionReason(i == 0 ? "AUTO_TOP_CONFIDENCE" : null);
      entity.setRawPayload(candidate.rawPayload());
      entity.setRawPayloadRef(candidate.provider() + ":" + candidate.providerPlanKey());
      FloorPlanCandidateEntity savedCandidate = floorPlanCandidateRepository.save(entity);

      NormalizedFloorPlanEntity normalized = new NormalizedFloorPlanEntity();
      normalized.setFloorPlanCandidateId(savedCandidate.getId());
      normalized.setNormalizationStatus(NormalizationStatus.READY);
      normalized.setPlanJson(candidate.normalizedPlan());
      normalized.setUncertaintyJson(Map.of("source", candidate.source()));
      normalized.setManualCheckItems(candidate.manualCheckItems());
      normalized.setNormalizedBy(NormalizedBy.RULE_ENGINE);
      NormalizedFloorPlanEntity savedNormalized = normalizedFloorPlanRepository.save(normalized);

      savedCandidate.setNormalizedPlanRef(savedNormalized.getId().toString());
      floorPlanCandidateRepository.save(savedCandidate);
    }

    return new FloorPlanResolveResponse("COMPLETED", candidates.size());
  }

  public FloorPlanListResponse list(String projectId) {
    UUID parsedProjectId = UUID.fromString(projectId);
    List<FloorPlanCandidateEntity> candidates =
        floorPlanCandidateRepository.findByProjectIdOrderByConfidenceScoreDesc(parsedProjectId);

    String selectedPlanId =
        candidates.stream()
            .filter(FloorPlanCandidateEntity::isSelected)
            .findFirst()
            .map(candidate -> candidate.getId().toString())
            .orElse(null);

    List<FloorPlanController.FloorPlanCandidateResponse> response =
        candidates.stream()
            .map(
                candidate -> {
                  FloorPlanSourceEntity source =
                      floorPlanSourceRepository
                          .findById(candidate.getFloorPlanSourceId())
                          .orElseThrow();
                  List<String> manualCheckItems =
                      normalizedFloorPlanRepository
                          .findByFloorPlanCandidateId(candidate.getId())
                          .map(NormalizedFloorPlanEntity::getManualCheckItems)
                          .orElse(List.of());
                  return FloorPlanMapper.toResponse(candidate, source, manualCheckItems);
                })
            .toList();

    return new FloorPlanListResponse(selectedPlanId, response);
  }

  @Transactional
  public FloorPlanSelectResponse select(
      String projectId, String candidateId, FloorPlanSelectRequest request) {
    UUID parsedProjectId = UUID.fromString(projectId);
    UUID parsedCandidateId = UUID.fromString(candidateId);

    floorPlanCandidateRepository
        .findByProjectIdAndId(parsedProjectId, parsedCandidateId)
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.FLOOR_PLAN_NOT_FOUND, HttpStatus.NOT_FOUND, "도면 후보를 찾을 수 없습니다."));

    List<FloorPlanCandidateEntity> candidates =
        floorPlanCandidateRepository.findByProjectId(parsedProjectId);
    for (FloorPlanCandidateEntity candidate : candidates) {
      boolean isSelected = candidate.getId().equals(parsedCandidateId);
      candidate.setSelected(isSelected);
      candidate.setSelectionReason(isSelected ? request.reason() : null);
    }
    floorPlanCandidateRepository.saveAll(candidates);

    return new FloorPlanSelectResponse(candidateId);
  }

  private List<FloorPlanProviderCandidate> callProvider(
      String provider,
      String operation,
      ProjectEntity project,
      PropertyEntity property,
      CandidateCall candidateCall) {
    long start = System.currentTimeMillis();
    try {
      List<FloorPlanProviderCandidate> result = candidateCall.call();
      integrationLogService.logSuccess(
          provider,
          operation,
          Map.of(
              "projectId", project.getId().toString(), "propertyId", property.getId().toString()),
          Map.of("count", result.size()),
          System.currentTimeMillis() - start);
      return result;
    } catch (RuntimeException exception) {
      integrationLogService.logFailure(
          provider,
          operation,
          Map.of(
              "projectId", project.getId().toString(), "propertyId", property.getId().toString()),
          exception.getMessage(),
          System.currentTimeMillis() - start);
      return List.of();
    }
  }

  @FunctionalInterface
  interface CandidateCall {
    List<FloorPlanProviderCandidate> call();
  }
}
