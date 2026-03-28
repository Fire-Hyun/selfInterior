package com.selfinterior.api.visualqa;

import com.selfinterior.api.common.api.ApiException;
import com.selfinterior.api.common.api.ErrorCode;
import com.selfinterior.api.project.ProjectEntity;
import com.selfinterior.api.project.ProjectRepository;
import com.selfinterior.api.visualqa.VisualQuestionController.CreateVisualQuestionResponse;
import com.selfinterior.api.visualqa.VisualQuestionController.GuideLinkResponse;
import com.selfinterior.api.visualqa.VisualQuestionController.VisualQuestionDetailResponse;
import com.selfinterior.api.visualqa.VisualQuestionController.VisualQuestionListResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class VisualQuestionService {
  private final ProjectRepository projectRepository;
  private final VisualQuestionRepository visualQuestionRepository;
  private final VisualQuestionImageRepository visualQuestionImageRepository;
  private final VisualAnswerRepository visualAnswerRepository;
  private final VisualQuestionStorage visualQuestionStorage;
  private final VisionQaClient visionQaClient;

  @Transactional
  public CreateVisualQuestionResponse create(
      String projectId,
      String questionText,
      String processStepKey,
      SpaceType spaceType,
      List<MultipartFile> files) {
    ProjectEntity project = findProject(projectId);
    if (files == null || files.isEmpty()) {
      throw new ApiException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "최소 1장의 사진이 필요합니다.");
    }

    VisualQuestionEntity question = new VisualQuestionEntity();
    question.setProjectId(project.getId());
    question.setQuestionText(questionText);
    question.setProcessStepKey(resolveProcessStepKey(project, processStepKey));
    question.setSpaceType(spaceType);
    question.setStatus(VisualQuestionStatus.PROCESSING);
    VisualQuestionEntity savedQuestion = visualQuestionRepository.save(question);

    List<String> storedPaths = new ArrayList<>();
    for (MultipartFile file : files) {
      storedPaths.add(storeImage(savedQuestion.getId(), file));
    }

    VisionQaClient.VisionQaResult result =
        visionQaClient.analyze(
            savedQuestion.getQuestionText(),
            savedQuestion.getProcessStepKey(),
            savedQuestion.getSpaceType(),
            storedPaths);

    VisualAnswerEntity answer = new VisualAnswerEntity();
    answer.setQuestionId(savedQuestion.getId());
    answer.setRiskLevel(result.riskLevel());
    answer.setObservedText(result.observedText());
    answer.setPossibleCausesText(result.possibleCausesText());
    answer.setNextChecksText(result.nextChecksText());
    answer.setProceedRecommendationText(result.proceedRecommendationText());
    answer.setExpertRequired(result.expertRequired());
    answer.setConfidenceScore(BigDecimal.valueOf(result.confidenceScore()));
    visualAnswerRepository.save(answer);

    savedQuestion.setStatus(VisualQuestionStatus.COMPLETED);
    visualQuestionRepository.save(savedQuestion);

    return new CreateVisualQuestionResponse(
        savedQuestion.getId().toString(), savedQuestion.getStatus().name());
  }

  public VisualQuestionListResponse list(String projectId) {
    ProjectEntity project = findProject(projectId);
    List<VisualQuestionEntity> questions =
        visualQuestionRepository.findByProjectIdOrderByCreatedAtDesc(project.getId());

    return new VisualQuestionListResponse(
        questions.stream()
            .map(
                question ->
                    VisualQuestionMapper.toSummary(
                        question,
                        visualAnswerRepository.findByQuestionId(question.getId()).orElse(null)))
            .toList());
  }

  public VisualQuestionDetailResponse get(String projectId, String questionId) {
    ProjectEntity project = findProject(projectId);
    VisualQuestionEntity question =
        visualQuestionRepository
            .findById(UUID.fromString(questionId))
            .filter(found -> found.getProjectId().equals(project.getId()))
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.VISUAL_QUESTION_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "사진 질문을 찾을 수 없습니다."));
    VisualAnswerEntity answer =
        visualAnswerRepository
            .findByQuestionId(question.getId())
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.VISUAL_QUESTION_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "사진 질문 답변을 찾을 수 없습니다."));
    List<VisualQuestionImageEntity> images =
        visualQuestionImageRepository.findByQuestionIdOrderByCreatedAtAsc(question.getId());

    return VisualQuestionMapper.toDetail(
        question, answer, images, buildGuideLinks(project, question));
  }

  private ProjectEntity findProject(String projectId) {
    return projectRepository
        .findById(UUID.fromString(projectId))
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
  }

  private String resolveProcessStepKey(ProjectEntity project, String processStepKey) {
    if (processStepKey != null && !processStepKey.isBlank()) {
      return processStepKey;
    }
    return project.getCurrentProcessStep();
  }

  private String storeImage(UUID questionId, MultipartFile file) {
    try {
      VisualQuestionStorage.StoredVisualQuestionFile stored =
          visualQuestionStorage.store(questionId, file);
      VisualQuestionImageEntity image = new VisualQuestionImageEntity();
      image.setQuestionId(questionId);
      image.setFileName(stored.fileName());
      image.setContentType(stored.contentType());
      image.setStoragePath(stored.storagePath());
      visualQuestionImageRepository.save(image);
      return stored.storagePath();
    } catch (IOException exception) {
      throw new ApiException(
          ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "질문 이미지를 저장하지 못했습니다.");
    }
  }

  private List<GuideLinkResponse> buildGuideLinks(
      ProjectEntity project, VisualQuestionEntity question) {
    if (question.getProcessStepKey() == null || question.getProcessStepKey().isBlank()) {
      return List.of(
          new GuideLinkResponse(
              "프로젝트 홈에서 현재 단계를 다시 확인하기", "project-home", "/projects/" + project.getId() + "/home"));
    }

    return List.of(
        new GuideLinkResponse(
            question.getProcessStepKey() + " 체크리스트 다시 보기",
            "process-" + question.getProcessStepKey().toLowerCase(),
            "/projects/" + project.getId() + "/process"));
  }
}
