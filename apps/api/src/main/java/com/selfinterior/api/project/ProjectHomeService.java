package com.selfinterior.api.project;

import com.selfinterior.api.common.api.ApiException;
import com.selfinterior.api.common.api.ErrorCode;
import com.selfinterior.api.floorplan.FloorPlanCandidateEntity;
import com.selfinterior.api.floorplan.FloorPlanCandidateRepository;
import com.selfinterior.api.floorplan.FloorPlanSourceEntity;
import com.selfinterior.api.floorplan.FloorPlanSourceRepository;
import com.selfinterior.api.floorplan.NormalizedFloorPlanEntity;
import com.selfinterior.api.floorplan.NormalizedFloorPlanRepository;
import com.selfinterior.api.process.ProcessPlanStatus;
import com.selfinterior.api.process.ProjectProcessPlanEntity;
import com.selfinterior.api.process.ProjectProcessPlanRepository;
import com.selfinterior.api.process.ProjectProcessStepEntity;
import com.selfinterior.api.process.ProjectProcessStepRepository;
import com.selfinterior.api.project.ProjectController.HomeActionResponse;
import com.selfinterior.api.project.ProjectController.HomePlaceholderCardResponse;
import com.selfinterior.api.project.ProjectController.ProjectHomeResponse;
import com.selfinterior.api.property.PropertyEntity;
import com.selfinterior.api.property.PropertyRepository;
import com.selfinterior.api.visualqa.VisualAnswerEntity;
import com.selfinterior.api.visualqa.VisualAnswerRepository;
import com.selfinterior.api.visualqa.VisualQuestionEntity;
import com.selfinterior.api.visualqa.VisualQuestionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectHomeService {
  private final ProjectRepository projectRepository;
  private final PropertyRepository propertyRepository;
  private final FloorPlanCandidateRepository floorPlanCandidateRepository;
  private final FloorPlanSourceRepository floorPlanSourceRepository;
  private final NormalizedFloorPlanRepository normalizedFloorPlanRepository;
  private final ProjectProcessPlanRepository projectProcessPlanRepository;
  private final ProjectProcessStepRepository projectProcessStepRepository;
  private final VisualQuestionRepository visualQuestionRepository;
  private final VisualAnswerRepository visualAnswerRepository;

  public ProjectHomeResponse get(String projectId) {
    ProjectEntity project = findProject(projectId);
    PropertyEntity property = propertyRepository.findByProjectId(project.getId()).orElse(null);
    List<FloorPlanCandidateEntity> candidates =
        floorPlanCandidateRepository.findByProjectIdOrderByConfidenceScoreDesc(project.getId());
    FloorPlanCandidateEntity selectedCandidate =
        candidates.stream().filter(FloorPlanCandidateEntity::isSelected).findFirst().orElse(null);
    FloorPlanSourceEntity selectedSource =
        selectedCandidate == null
            ? null
            : floorPlanSourceRepository
                .findById(selectedCandidate.getFloorPlanSourceId())
                .orElse(null);
    List<String> manualCheckItems = loadManualCheckItems(selectedCandidate);
    ProjectProcessPlanEntity processPlan =
        projectProcessPlanRepository.findByProjectId(project.getId()).orElse(null);
    ProjectProcessStepEntity currentProcessStep = loadCurrentProcessStep(processPlan);
    VisualQuestionEntity latestQuestion = loadLatestQuestion(project.getId());
    VisualAnswerEntity latestAnswer =
        latestQuestion == null
            ? null
            : visualAnswerRepository.findByQuestionId(latestQuestion.getId()).orElse(null);

    return new ProjectHomeResponse(
        ProjectMapper.toHomeProject(project),
        ProjectMapper.toHomeProperty(property),
        ProjectMapper.toHomeFloorPlan(
            selectedCandidate, selectedSource, candidates.size(), manualCheckItems),
        buildNextActions(
            project.getId(),
            property,
            selectedCandidate,
            manualCheckItems,
            processPlan,
            currentProcessStep),
        buildRecentQuestionCard(project.getId(), latestQuestion, latestAnswer),
        buildPlaceholderCard(
            "추천 전문가",
            "PENDING_INTEGRATION",
            "ExpertLead 도메인이 연결되면 현재 단계와 질문 이력을 반영한 추천 전문가가 여기에 표시됩니다.",
            "전문가 모듈 준비 중",
            "/projects/" + project.getId() + "/home#recommended-experts"));
  }

  private ProjectEntity findProject(String projectId) {
    return projectRepository
        .findById(UUID.fromString(projectId))
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
  }

  private List<String> loadManualCheckItems(FloorPlanCandidateEntity selectedCandidate) {
    if (selectedCandidate == null) {
      return List.of();
    }

    return normalizedFloorPlanRepository
        .findByFloorPlanCandidateId(selectedCandidate.getId())
        .map(NormalizedFloorPlanEntity::getManualCheckItems)
        .orElse(List.of());
  }

  private List<HomeActionResponse> buildNextActions(
      UUID projectId,
      PropertyEntity property,
      FloorPlanCandidateEntity selectedCandidate,
      List<String> manualCheckItems,
      ProjectProcessPlanEntity processPlan,
      ProjectProcessStepEntity currentProcessStep) {
    List<HomeActionResponse> actions = new ArrayList<>();

    actions.add(
        property == null
            ? new HomeActionResponse(
                "PROPERTY_SETUP",
                "집 정보 연결 마무리",
                "프로젝트 홈을 완성하려면 주소와 집 요약을 먼저 연결해야 합니다.",
                "READY",
                "/projects/" + projectId + "/home#property")
            : new HomeActionResponse(
                "PROPERTY_SETUP",
                "집 정보 연결 완료",
                "집 기본 정보가 연결되어 도면과 후속 모듈의 기준으로 사용됩니다.",
                "DONE",
                "/projects/" + projectId + "/home#property"));

    if (selectedCandidate == null) {
      actions.add(
          new HomeActionResponse(
              "FLOOR_PLAN_REVIEW",
              "도면 후보 확인",
              property == null
                  ? "집 정보가 연결되면 도면 후보를 찾고 검토할 수 있습니다."
                  : "도면 후보를 선택해 이후 공정 계획의 기준 구조를 고정하세요.",
              property == null ? "BLOCKED" : "READY",
              "/projects/" + projectId + "/home#plan"));
    } else if (!manualCheckItems.isEmpty()) {
      actions.add(
          new HomeActionResponse(
              "PLAN_MEASURE_CHECK",
              "실측 확인 항목 검토",
              manualCheckItems.size() == 1
                  ? manualCheckItems.get(0)
                  : manualCheckItems.get(0) + " 외 " + (manualCheckItems.size() - 1) + "건",
              "READY",
              "/projects/" + projectId + "/home#plan"));
    } else {
      actions.add(
          new HomeActionResponse(
              "FLOOR_PLAN_REVIEW",
              "도면 후보 확인 완료",
              "선택된 도면 후보가 프로젝트 기준 구조로 반영되었습니다.",
              "DONE",
              "/projects/" + projectId + "/home#plan"));
    }

    if (processPlan != null && currentProcessStep != null) {
      actions.add(
          new HomeActionResponse(
              "PROCESS_PLAN",
              processPlan.getPlanStatus() == ProcessPlanStatus.COMPLETED
                  ? "공정 플랜 완료"
                  : "현재 공정 체크리스트 검토",
              processPlan.getPlanStatus() == ProcessPlanStatus.COMPLETED
                  ? "모든 공정 체크리스트가 완료되었습니다."
                  : currentProcessStep.getTitle() + " 단계의 체크리스트를 이어서 확인하세요.",
              processPlan.getPlanStatus() == ProcessPlanStatus.COMPLETED ? "DONE" : "READY",
              "/projects/" + projectId + "/process"));
    } else {
      actions.add(
          new HomeActionResponse(
              "PROCESS_PLAN",
              "공정 플랜 생성",
              selectedCandidate == null
                  ? "도면 기준이 정해지면 공정 플랜을 생성할 수 있습니다."
                  : "선택된 도면 후보를 기준으로 프로젝트별 공정 초안을 만들 수 있습니다.",
              selectedCandidate == null ? "BLOCKED" : "READY",
              "/projects/" + projectId + "/process"));
    }

    return actions;
  }

  private ProjectProcessStepEntity loadCurrentProcessStep(ProjectProcessPlanEntity processPlan) {
    if (processPlan == null || processPlan.getCurrentStepKey() == null) {
      return null;
    }

    return projectProcessStepRepository
        .findByProcessPlanIdAndStepKey(processPlan.getId(), processPlan.getCurrentStepKey())
        .orElse(null);
  }

  private VisualQuestionEntity loadLatestQuestion(UUID projectId) {
    List<VisualQuestionEntity> questions =
        visualQuestionRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    return questions.isEmpty() ? null : questions.get(0);
  }

  private HomePlaceholderCardResponse buildRecentQuestionCard(
      UUID projectId, VisualQuestionEntity latestQuestion, VisualAnswerEntity latestAnswer) {
    if (latestQuestion == null) {
      return buildPlaceholderCard(
          "최근 질문",
          "PENDING_INTEGRATION",
          "VisualQuestion 도메인이 연결되면 현장 사진 질문과 답변 상태가 여기에 표시됩니다.",
          "질문 모듈 준비 중",
          "/projects/" + projectId + "/home#recent-questions");
    }

    return new HomePlaceholderCardResponse(
        "최근 질문",
        latestAnswer == null
            ? latestQuestion.getStatus().name()
            : latestAnswer.getRiskLevel().name(),
        latestQuestion.getQuestionText(),
        latestAnswer != null && latestAnswer.isExpertRequired() ? "답변과 위험도 보기" : "질문 상세 보기",
        "/projects/" + projectId + "/qa");
  }

  private HomePlaceholderCardResponse buildPlaceholderCard(
      String title,
      String status,
      String description,
      String primaryActionLabel,
      String primaryActionPath) {
    return new HomePlaceholderCardResponse(
        title, status, description, primaryActionLabel, primaryActionPath);
  }
}
