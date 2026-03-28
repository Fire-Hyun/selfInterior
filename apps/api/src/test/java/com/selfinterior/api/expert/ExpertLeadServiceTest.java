package com.selfinterior.api.expert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.selfinterior.api.project.ProjectEntity;
import com.selfinterior.api.project.ProjectRepository;
import com.selfinterior.api.project.ProjectType;
import com.selfinterior.api.property.PropertyEntity;
import com.selfinterior.api.property.PropertyRepository;
import com.selfinterior.api.visualqa.RiskLevel;
import com.selfinterior.api.visualqa.SpaceType;
import com.selfinterior.api.visualqa.VisualAnswerEntity;
import com.selfinterior.api.visualqa.VisualAnswerRepository;
import com.selfinterior.api.visualqa.VisualQuestionEntity;
import com.selfinterior.api.visualqa.VisualQuestionImageEntity;
import com.selfinterior.api.visualqa.VisualQuestionImageRepository;
import com.selfinterior.api.visualqa.VisualQuestionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpertLeadServiceTest {
  @Mock private ExpertCategoryRepository expertCategoryRepository;
  @Mock private ExpertRepository expertRepository;
  @Mock private ExpertCategoryLinkRepository expertCategoryLinkRepository;
  @Mock private ExpertServiceRegionRepository expertServiceRegionRepository;
  @Mock private ExpertPortfolioRepository expertPortfolioRepository;
  @Mock private ExpertLeadRepository expertLeadRepository;
  @Mock private ExpertLeadEventRepository expertLeadEventRepository;
  @Mock private ProjectRepository projectRepository;
  @Mock private PropertyRepository propertyRepository;
  @Mock private VisualQuestionRepository visualQuestionRepository;
  @Mock private VisualAnswerRepository visualAnswerRepository;
  @Mock private VisualQuestionImageRepository visualQuestionImageRepository;

  @InjectMocks private ExpertLeadService expertLeadService;

  @Test
  void getRecommendationsUsesHighRiskQuestionContext() {
    UUID projectId = UUID.randomUUID();
    UUID expertId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    UUID questionId = UUID.randomUUID();

    ProjectEntity project = new ProjectEntity();
    project.setId(projectId);
    project.setProjectType(ProjectType.PARTIAL);
    project.setBudgetMin(500000);
    project.setBudgetMax(4000000);
    project.setCurrentProcessStep("ELECTRICAL");

    PropertyEntity property = new PropertyEntity();
    property.setProjectId(projectId);
    property.setSido("서울특별시");
    property.setSigungu("강남구");

    VisualQuestionEntity question = new VisualQuestionEntity();
    question.setId(questionId);
    question.setProjectId(projectId);
    question.setQuestionText("콘센트 쪽이 뜨거운데 전기 문제일까요?");
    question.setSpaceType(SpaceType.KITCHEN);

    VisualAnswerEntity answer = new VisualAnswerEntity();
    answer.setQuestionId(questionId);
    answer.setRiskLevel(RiskLevel.HIGH);
    answer.setObservedText("콘센트 주변 발열 흔적이 보입니다.");

    ExpertCategoryEntity category = new ExpertCategoryEntity();
    category.setId(categoryId);
    category.setKey("ELECTRICAL");
    category.setName("전기 점검");
    category.setActive(true);

    ExpertEntity expert = new ExpertEntity();
    expert.setId(expertId);
    expert.setCompanyName("세운 전기 설비");
    expert.setContactName("박지훈");
    expert.setMinBudget(500000);
    expert.setMaxBudget(5000000);
    expert.setPartialWorkSupported(true);
    expert.setSemiSelfCollaborationSupported(true);
    expert.setResponseScore(BigDecimal.valueOf(95.0));
    expert.setReviewScore(BigDecimal.valueOf(4.9));
    expert.setStatus(ExpertStatus.ACTIVE);

    ExpertCategoryLinkEntity link = new ExpertCategoryLinkEntity();
    link.setExpertId(expertId);
    link.setExpertCategoryId(categoryId);

    ExpertServiceRegionEntity region = new ExpertServiceRegionEntity();
    region.setExpertId(expertId);
    region.setSido("서울특별시");
    region.setSigungu("강남구");

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(propertyRepository.findByProjectId(projectId)).thenReturn(Optional.of(property));
    when(visualQuestionRepository.findByProjectIdOrderByCreatedAtDesc(projectId))
        .thenReturn(List.of(question));
    when(visualAnswerRepository.findByQuestionId(questionId)).thenReturn(Optional.of(answer));
    when(expertCategoryRepository.findByKeyAndActiveTrue("ELECTRICAL"))
        .thenReturn(Optional.of(category));
    when(expertCategoryRepository.findAll()).thenReturn(List.of(category));
    when(expertRepository.findByStatus(ExpertStatus.ACTIVE)).thenReturn(List.of(expert));
    when(expertCategoryLinkRepository.findByExpertIdIn(List.of(expertId)))
        .thenReturn(List.of(link));
    when(expertServiceRegionRepository.findByExpertIdIn(List.of(expertId)))
        .thenReturn(List.of(region));
    when(expertPortfolioRepository.findByExpertId(expertId)).thenReturn(List.of());

    var response = expertLeadService.getRecommendations(projectId.toString());

    assertThat(response.primaryCategoryKey()).isEqualTo("ELECTRICAL");
    assertThat(response.experts()).hasSize(1);
    assertThat(response.experts().get(0).companyName()).isEqualTo("세운 전기 설비");
    assertThat(response.experts().get(0).recommendationReason()).contains("전기 점검");
  }

  @Test
  void createLeadStoresAttachmentPayloadAndEvent() {
    UUID projectId = UUID.randomUUID();
    UUID ownerUserId = UUID.randomUUID();
    UUID expertId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    UUID questionId = UUID.randomUUID();
    UUID savedLeadId = UUID.randomUUID();

    ProjectEntity project = new ProjectEntity();
    project.setId(projectId);
    project.setOwnerUserId(ownerUserId);
    project.setTitle("잠실 프로젝트");
    project.setProjectType(ProjectType.PARTIAL);
    project.setBudgetMin(1000000);
    project.setBudgetMax(5000000);
    project.setCurrentProcessStep("ISSUE_DIAGNOSIS");

    PropertyEntity property = new PropertyEntity();
    property.setProjectId(projectId);
    property.setRoadAddress("서울특별시 강남구 테헤란로 1");
    property.setSido("서울특별시");
    property.setSigungu("강남구");
    property.setApartmentName("테스트 아파트");
    property.setExclusiveAreaM2(BigDecimal.valueOf(84.9));

    VisualQuestionEntity question = new VisualQuestionEntity();
    question.setId(questionId);
    question.setProjectId(projectId);
    question.setQuestionText("욕실에 누수 흔적이 있습니다.");
    question.setProcessStepKey("ISSUE_DIAGNOSIS");
    question.setSpaceType(SpaceType.BATHROOM);

    VisualAnswerEntity answer = new VisualAnswerEntity();
    answer.setQuestionId(questionId);
    answer.setRiskLevel(RiskLevel.HIGH);
    answer.setObservedText("실리콘 주변 누수 흔적이 보입니다.");
    answer.setExpertRequired(true);

    VisualQuestionImageEntity image = new VisualQuestionImageEntity();
    image.setQuestionId(questionId);
    image.setStoragePath("tmp/visual-questions/q1/bathroom.jpg");

    ExpertCategoryEntity category = new ExpertCategoryEntity();
    category.setId(categoryId);
    category.setKey("WATERPROOFING");
    category.setName("누수/방수 점검");
    category.setActive(true);

    ExpertEntity expert = new ExpertEntity();
    expert.setId(expertId);
    expert.setCompanyName("한빛 누수 솔루션");
    expert.setContactName("김도윤");
    expert.setStatus(ExpertStatus.ACTIVE);
    expert.setPartialWorkSupported(true);
    expert.setSemiSelfCollaborationSupported(true);

    ExpertCategoryLinkEntity link = new ExpertCategoryLinkEntity();
    link.setExpertId(expertId);
    link.setExpertCategoryId(categoryId);

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(propertyRepository.findByProjectId(projectId)).thenReturn(Optional.of(property));
    when(visualQuestionRepository.findByProjectIdOrderByCreatedAtDesc(projectId))
        .thenReturn(List.of(question));
    when(visualAnswerRepository.findByQuestionId(questionId)).thenReturn(Optional.of(answer));
    when(visualQuestionImageRepository.findByQuestionIdOrderByCreatedAtAsc(questionId))
        .thenReturn(List.of(image));
    when(expertCategoryRepository.findByKeyAndActiveTrue("WATERPROOFING"))
        .thenReturn(Optional.of(category));
    when(expertCategoryRepository.findAll()).thenReturn(List.of(category));
    when(expertRepository.findByStatus(ExpertStatus.ACTIVE)).thenReturn(List.of(expert));
    when(expertCategoryLinkRepository.findByExpertIdIn(List.of(expertId)))
        .thenReturn(List.of(link));
    when(expertServiceRegionRepository.findByExpertIdIn(List.of(expertId))).thenReturn(List.of());
    when(expertPortfolioRepository.findByExpertId(expertId)).thenReturn(List.of());
    when(expertLeadRepository.save(any()))
        .thenAnswer(
            invocation -> {
              ExpertLeadEntity entity = invocation.getArgument(0);
              entity.setId(savedLeadId);
              return entity;
            });
    when(expertLeadEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        expertLeadService.createLead(
            projectId.toString(),
            new ExpertController.CreateExpertLeadRequest(
                expertId.toString(),
                "WATERPROOFING",
                1200000,
                4000000,
                LocalDate.of(2026, 4, 20),
                "욕실 누수 원인 점검과 최소 범위 보수를 문의합니다."));

    ArgumentCaptor<ExpertLeadEntity> leadCaptor = ArgumentCaptor.forClass(ExpertLeadEntity.class);
    org.mockito.Mockito.verify(expertLeadRepository).save(leadCaptor.capture());
    Map<String, Object> payload = leadCaptor.getValue().getAttachmentPayload();

    assertThat(response.leadId()).isEqualTo(savedLeadId.toString());
    assertThat(response.leadStatus()).isEqualTo("NEW");
    assertThat(payload).containsKey("project");
    assertThat(payload).containsKey("latestQuestion");
    assertThat(payload.get("latestQuestion").toString()).contains("욕실에 누수 흔적");
    assertThat(payload.get("request").toString()).contains("WATERPROOFING");
  }
}
