package com.selfinterior.api.visualqa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.selfinterior.api.project.ProjectEntity;
import com.selfinterior.api.project.ProjectRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class VisualQuestionServiceTest {
  @Mock private ProjectRepository projectRepository;
  @Mock private VisualQuestionRepository visualQuestionRepository;
  @Mock private VisualQuestionImageRepository visualQuestionImageRepository;
  @Mock private VisualAnswerRepository visualAnswerRepository;
  @Mock private VisualQuestionStorage visualQuestionStorage;
  @Mock private VisionQaClient visionQaClient;

  @InjectMocks private VisualQuestionService visualQuestionService;

  @Test
  void createStoresImagesAndAnswer() throws IOException {
    UUID projectId = UUID.randomUUID();
    UUID questionId = UUID.randomUUID();

    ProjectEntity project = new ProjectEntity();
    project.setId(projectId);
    project.setCurrentProcessStep("SURFACE_FINISH");

    MockMultipartFile file =
        new MockMultipartFile("files", "wall.jpg", "image/jpeg", "demo".getBytes());

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(visualQuestionRepository.save(any()))
        .thenAnswer(
            invocation -> {
              VisualQuestionEntity entity = invocation.getArgument(0);
              if (entity.getId() == null) {
                entity.setId(questionId);
              }
              return entity;
            });
    when(visualQuestionStorage.store(questionId, file))
        .thenReturn(
            new VisualQuestionStorage.StoredVisualQuestionFile(
                "wall.jpg", "image/jpeg", "tmp/visual-questions/" + questionId + "/wall.jpg"));
    when(visionQaClient.analyze(any(), any(), any(), any()))
        .thenReturn(
            new VisionQaClient.VisionQaResult(
                RiskLevel.HIGH,
                "욕실 벽면 변색이 보입니다.",
                "누수 또는 결로 가능성이 있습니다.",
                "상부 배관과 실리콘 상태를 확인하세요.",
                "원인 확인 전 마감 공정 진행은 보수 비용을 키울 수 있습니다.",
                true,
                77.2));
    when(visualAnswerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(visualQuestionImageRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        visualQuestionService.create(
            projectId.toString(), "여기 누수인가요?", null, SpaceType.BATHROOM, List.of(file));

    assertThat(response.questionId()).isEqualTo(questionId.toString());
    assertThat(response.status()).isEqualTo("COMPLETED");
  }

  @Test
  void getReturnsDetailResponse() {
    UUID projectId = UUID.randomUUID();
    UUID questionId = UUID.randomUUID();
    UUID imageId = UUID.randomUUID();

    ProjectEntity project = new ProjectEntity();
    project.setId(projectId);

    VisualQuestionEntity question = new VisualQuestionEntity();
    question.setId(questionId);
    question.setProjectId(projectId);
    question.setQuestionText("이 부분 상태가 괜찮은가요?");
    question.setProcessStepKey("ELECTRICAL");
    question.setSpaceType(SpaceType.KITCHEN);
    question.setStatus(VisualQuestionStatus.COMPLETED);

    VisualAnswerEntity answer = new VisualAnswerEntity();
    answer.setQuestionId(questionId);
    answer.setRiskLevel(RiskLevel.MEDIUM);
    answer.setObservedText("가전 주변 마감 상태가 보입니다.");
    answer.setPossibleCausesText("사용량 증가와 마감 노후가 함께 영향을 줄 수 있습니다.");
    answer.setNextChecksText("차단기 이력과 발열 여부를 확인하세요.");
    answer.setProceedRecommendationText("추가 확인 후 공정을 진행하세요.");
    answer.setExpertRequired(false);
    answer.setConfidenceScore(BigDecimal.valueOf(64.5));

    VisualQuestionImageEntity image = new VisualQuestionImageEntity();
    image.setId(imageId);
    image.setQuestionId(questionId);
    image.setFileName("kitchen.jpg");
    image.setStoragePath("tmp/visual-questions/" + questionId + "/kitchen.jpg");

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(visualQuestionRepository.findById(questionId)).thenReturn(Optional.of(question));
    when(visualAnswerRepository.findByQuestionId(questionId)).thenReturn(Optional.of(answer));
    when(visualQuestionImageRepository.findByQuestionIdOrderByCreatedAtAsc(questionId))
        .thenReturn(List.of(image));

    var response = visualQuestionService.get(projectId.toString(), questionId.toString());

    assertThat(response.question().questionText()).isEqualTo("이 부분 상태가 괜찮은가요?");
    assertThat(response.answer().riskLevel()).isEqualTo("MEDIUM");
    assertThat(response.images()).hasSize(1);
    assertThat(response.relatedGuideLinks()).hasSize(1);
  }
}
