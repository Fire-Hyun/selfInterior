package com.selfinterior.api.floorplan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.selfinterior.api.floorplan.FloorPlanController.FloorPlanSelectRequest;
import com.selfinterior.api.floorplan.provider.ApproximateFloorPlanGenerator;
import com.selfinterior.api.floorplan.provider.LicensedFloorPlanClient;
import com.selfinterior.api.floorplan.provider.OfficialFloorPlanClient;
import com.selfinterior.api.integration.IntegrationLogService;
import com.selfinterior.api.project.ProjectRepository;
import com.selfinterior.api.property.PropertyRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FloorPlanResolutionServiceTest {
  @Mock private ProjectRepository projectRepository;
  @Mock private PropertyRepository propertyRepository;
  @Mock private FloorPlanSourceRepository floorPlanSourceRepository;
  @Mock private FloorPlanCandidateRepository floorPlanCandidateRepository;
  @Mock private NormalizedFloorPlanRepository normalizedFloorPlanRepository;
  @Mock private OfficialFloorPlanClient officialFloorPlanClient;
  @Mock private LicensedFloorPlanClient licensedFloorPlanClient;
  @Mock private ApproximateFloorPlanGenerator approximateFloorPlanGenerator;
  @Mock private IntegrationLogService integrationLogService;

  @InjectMocks private FloorPlanResolutionService floorPlanResolutionService;

  @Test
  void selectMarksOnlyRequestedCandidateAsSelected() {
    UUID projectId = UUID.randomUUID();
    UUID selectedCandidateId = UUID.randomUUID();

    FloorPlanCandidateEntity first = new FloorPlanCandidateEntity();
    first.setId(selectedCandidateId);
    first.setProjectId(projectId);
    first.setSelected(false);

    FloorPlanCandidateEntity second = new FloorPlanCandidateEntity();
    second.setId(UUID.randomUUID());
    second.setProjectId(projectId);
    second.setSelected(true);

    when(floorPlanCandidateRepository.findByProjectIdAndId(projectId, selectedCandidateId))
        .thenReturn(Optional.of(first));
    when(floorPlanCandidateRepository.findByProjectId(projectId))
        .thenReturn(List.of(first, second));
    when(floorPlanCandidateRepository.saveAll(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        floorPlanResolutionService.select(
            projectId.toString(),
            selectedCandidateId.toString(),
            new FloorPlanSelectRequest("MOST_SIMILAR"));

    assertThat(response.selectedPlanId()).isEqualTo(selectedCandidateId.toString());
    assertThat(first.isSelected()).isTrue();
    assertThat(first.getSelectionReason()).isEqualTo("MOST_SIMILAR");
    assertThat(second.isSelected()).isFalse();
    assertThat(second.getSelectionReason()).isNull();
  }
}
