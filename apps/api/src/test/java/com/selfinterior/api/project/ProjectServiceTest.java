package com.selfinterior.api.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.selfinterior.api.floorplan.ConfidenceGrade;
import com.selfinterior.api.floorplan.FloorPlanCandidateEntity;
import com.selfinterior.api.floorplan.FloorPlanCandidateRepository;
import com.selfinterior.api.floorplan.FloorPlanSourceEntity;
import com.selfinterior.api.floorplan.FloorPlanSourceRepository;
import com.selfinterior.api.floorplan.FloorPlanSourceType;
import com.selfinterior.api.floorplan.LicenseStatus;
import com.selfinterior.api.project.ProjectController.CreateProjectRequest;
import com.selfinterior.api.property.PropertyEntity;
import com.selfinterior.api.property.PropertyRepository;
import com.selfinterior.api.property.PropertyType;
import com.selfinterior.api.user.UserEntity;
import com.selfinterior.api.user.UserRepository;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
  @Mock private ProjectRepository projectRepository;
  @Mock private ProjectMemberRepository projectMemberRepository;
  @Mock private ProjectScopeRepository projectScopeRepository;
  @Mock private PropertyRepository propertyRepository;
  @Mock private FloorPlanCandidateRepository floorPlanCandidateRepository;
  @Mock private FloorPlanSourceRepository floorPlanSourceRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private ProjectService projectService;

  private final UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @BeforeEach
  void setUp() throws Exception {
    Field field = ProjectService.class.getDeclaredField("defaultOwnerUserId");
    field.setAccessible(true);
    field.set(projectService, ownerId);
  }

  @Test
  void createBuildsProjectMemberAndScope() {
    UserEntity owner = new UserEntity();
    owner.setEmail("owner@selfinterior.local");
    owner.setProvider("seed");
    owner.setName("Default Owner");

    when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
    when(projectRepository.save(any()))
        .thenAnswer(
            invocation -> {
              ProjectEntity entity = invocation.getArgument(0);
              entity.setId(UUID.randomUUID());
              return entity;
            });
    when(projectMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(projectScopeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        projectService.create(
            new CreateProjectRequest(
                "Recents Interior", ProjectType.FULL, LivingStatus.BEFORE_MOVE_IN, 1, 2));

    assertThat(response.project().title()).isEqualTo("Recents Interior");
    assertThat(response.project().projectType()).isEqualTo("FULL");
    assertThat(response.project().propertyAttached()).isFalse();
  }

  @Test
  void getReturnsPropertyAndSelectedPlanSummary() {
    UUID projectId = UUID.randomUUID();
    UUID sourceId = UUID.randomUUID();
    UUID candidateId = UUID.randomUUID();

    ProjectEntity project = new ProjectEntity();
    project.setId(projectId);
    project.setTitle("Recents Interior");
    project.setProjectType(ProjectType.FULL);
    project.setLivingStatus(LivingStatus.BEFORE_MOVE_IN);

    PropertyEntity property = new PropertyEntity();
    property.setProjectId(projectId);
    property.setPropertyType(PropertyType.APARTMENT);
    property.setApartmentName("Recents");
    property.setRoadAddress("135 Olympic-ro");
    property.setDongNo("201");
    property.setHoNo("1203");
    property.setExclusiveAreaM2(BigDecimal.valueOf(84.99));
    property.setRoomCount(3);
    property.setBathroomCount(2);

    FloorPlanCandidateEntity candidate = new FloorPlanCandidateEntity();
    candidate.setId(candidateId);
    candidate.setProjectId(projectId);
    candidate.setFloorPlanSourceId(sourceId);
    candidate.setSourceType(FloorPlanSourceType.OFFICIAL);
    candidate.setConfidenceGrade(ConfidenceGrade.EXACT);
    candidate.setConfidenceScore(BigDecimal.valueOf(93.2));
    candidate.setLayoutLabel("84A");
    candidate.setSource("OFFICIAL_APPROVED_PLAN");
    candidate.setSelected(true);

    FloorPlanSourceEntity source = new FloorPlanSourceEntity();
    source.setId(sourceId);
    source.setLicenseStatus(LicenseStatus.APPROVED);

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(propertyRepository.findByProjectId(projectId)).thenReturn(Optional.of(property));
    when(floorPlanCandidateRepository.findByProjectIdAndSelectedTrue(projectId))
        .thenReturn(Optional.of(candidate));
    when(floorPlanSourceRepository.findById(sourceId)).thenReturn(Optional.of(source));
    when(floorPlanCandidateRepository.findByProjectId(projectId)).thenReturn(List.of(candidate));

    var response = projectService.get(projectId.toString());

    assertThat(response.property()).isNotNull();
    assertThat(response.property().apartmentName()).isEqualTo("Recents");
    assertThat(response.selectedFloorPlan()).isNotNull();
    assertThat(response.selectedFloorPlan().candidateId()).isEqualTo(candidateId.toString());
    assertThat(response.floorPlanCandidateCount()).isEqualTo(1);
  }
}
