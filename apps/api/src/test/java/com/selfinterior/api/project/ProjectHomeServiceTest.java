package com.selfinterior.api.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.selfinterior.api.floorplan.ConfidenceGrade;
import com.selfinterior.api.floorplan.FloorPlanCandidateEntity;
import com.selfinterior.api.floorplan.FloorPlanCandidateRepository;
import com.selfinterior.api.floorplan.FloorPlanSourceEntity;
import com.selfinterior.api.floorplan.FloorPlanSourceRepository;
import com.selfinterior.api.floorplan.FloorPlanSourceType;
import com.selfinterior.api.floorplan.LicenseStatus;
import com.selfinterior.api.floorplan.NormalizedFloorPlanEntity;
import com.selfinterior.api.floorplan.NormalizedFloorPlanRepository;
import com.selfinterior.api.process.ProjectProcessPlanRepository;
import com.selfinterior.api.process.ProjectProcessStepRepository;
import com.selfinterior.api.property.PropertyEntity;
import com.selfinterior.api.property.PropertyRepository;
import com.selfinterior.api.property.PropertyType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectHomeServiceTest {
  @Mock private ProjectRepository projectRepository;
  @Mock private PropertyRepository propertyRepository;
  @Mock private FloorPlanCandidateRepository floorPlanCandidateRepository;
  @Mock private FloorPlanSourceRepository floorPlanSourceRepository;
  @Mock private NormalizedFloorPlanRepository normalizedFloorPlanRepository;
  @Mock private ProjectProcessPlanRepository projectProcessPlanRepository;
  @Mock private ProjectProcessStepRepository projectProcessStepRepository;

  @InjectMocks private ProjectHomeService projectHomeService;

  @Test
  void getBuildsProjectHomeCards() {
    UUID projectId = UUID.randomUUID();
    UUID sourceId = UUID.randomUUID();
    UUID candidateId = UUID.randomUUID();

    ProjectEntity project = new ProjectEntity();
    project.setId(projectId);
    project.setTitle("잠실 리센츠 홈");
    project.setProjectType(ProjectType.FULL);
    project.setLivingStatus(LivingStatus.BEFORE_MOVE_IN);
    project.setOnboardingCompleted(true);

    PropertyEntity property = new PropertyEntity();
    property.setProjectId(projectId);
    property.setPropertyType(PropertyType.APARTMENT);
    property.setApartmentName("리센츠");
    property.setRoadAddress("서울 송파구 올림픽로 135");
    property.setDongNo("201");
    property.setHoNo("1203");
    property.setCompletionYear(2008);
    property.setHouseholdCount(5563);
    property.setExclusiveAreaM2(BigDecimal.valueOf(84.99));

    FloorPlanCandidateEntity candidate = new FloorPlanCandidateEntity();
    candidate.setId(candidateId);
    candidate.setProjectId(projectId);
    candidate.setFloorPlanSourceId(sourceId);
    candidate.setSourceType(FloorPlanSourceType.OFFICIAL);
    candidate.setConfidenceGrade(ConfidenceGrade.EXACT);
    candidate.setConfidenceScore(BigDecimal.valueOf(93.2));
    candidate.setLayoutLabel("84A");
    candidate.setSource("OFFICIAL_APPROVED_PLAN");
    candidate.setExclusiveAreaM2(BigDecimal.valueOf(84.99));
    candidate.setRoomCount(3);
    candidate.setBathroomCount(2);
    candidate.setSelected(true);

    FloorPlanSourceEntity source = new FloorPlanSourceEntity();
    source.setId(sourceId);
    source.setLicenseStatus(LicenseStatus.APPROVED);

    NormalizedFloorPlanEntity normalized = new NormalizedFloorPlanEntity();
    normalized.setManualCheckItems(List.of("거실 폭 실측", "욕실 배수구 위치 확인"));

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(propertyRepository.findByProjectId(projectId)).thenReturn(Optional.of(property));
    when(floorPlanCandidateRepository.findByProjectIdOrderByConfidenceScoreDesc(projectId))
        .thenReturn(List.of(candidate));
    when(floorPlanSourceRepository.findById(sourceId)).thenReturn(Optional.of(source));
    when(normalizedFloorPlanRepository.findByFloorPlanCandidateId(candidateId))
        .thenReturn(Optional.of(normalized));
    when(projectProcessPlanRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

    var response = projectHomeService.get(projectId.toString());

    assertThat(response.project().title()).isEqualTo("잠실 리센츠 홈");
    assertThat(response.property()).isNotNull();
    assertThat(response.property().apartmentName()).isEqualTo("리센츠");
    assertThat(response.floorPlan()).isNotNull();
    assertThat(response.floorPlan().candidateCount()).isEqualTo(1);
    assertThat(response.floorPlan().manualCheckItems()).containsExactly("거실 폭 실측", "욕실 배수구 위치 확인");
    assertThat(response.nextActions()).hasSize(3);
    assertThat(response.nextActions().get(0).status()).isEqualTo("DONE");
    assertThat(response.nextActions().get(1).key()).isEqualTo("PLAN_MEASURE_CHECK");
    assertThat(response.recentQuestions().status()).isEqualTo("PENDING_INTEGRATION");
    assertThat(response.recommendedExperts().status()).isEqualTo("PENDING_INTEGRATION");
  }

  @Test
  void getReturnsBlockedActionsWhenPropertyMissing() {
    UUID projectId = UUID.randomUUID();

    ProjectEntity project = new ProjectEntity();
    project.setId(projectId);
    project.setTitle("빈 프로젝트");
    project.setProjectType(ProjectType.PARTIAL);
    project.setLivingStatus(LivingStatus.OCCUPIED);

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(propertyRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
    when(floorPlanCandidateRepository.findByProjectIdOrderByConfidenceScoreDesc(projectId))
        .thenReturn(List.of());
    when(projectProcessPlanRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

    var response = projectHomeService.get(projectId.toString());

    assertThat(response.property()).isNull();
    assertThat(response.floorPlan()).isNull();
    assertThat(response.nextActions().get(0).status()).isEqualTo("READY");
    assertThat(response.nextActions().get(1).status()).isEqualTo("BLOCKED");
    assertThat(response.nextActions().get(2).status()).isEqualTo("BLOCKED");
  }
}
