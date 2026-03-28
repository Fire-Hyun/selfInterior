package com.selfinterior.api.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.selfinterior.api.common.api.ApiException;
import com.selfinterior.api.floorplan.FloorPlanCandidateEntity;
import com.selfinterior.api.floorplan.FloorPlanCandidateRepository;
import com.selfinterior.api.project.LivingStatus;
import com.selfinterior.api.project.ProjectEntity;
import com.selfinterior.api.project.ProjectRepository;
import com.selfinterior.api.project.ProjectType;
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
class ProcessPlanServiceTest {
  @Mock private ProjectRepository projectRepository;
  @Mock private FloorPlanCandidateRepository floorPlanCandidateRepository;
  @Mock private ProcessCatalogRepository processCatalogRepository;
  @Mock private ProcessGuideRepository processGuideRepository;
  @Mock private ProcessChecklistItemRepository processChecklistItemRepository;
  @Mock private ProjectProcessPlanRepository projectProcessPlanRepository;
  @Mock private ProjectProcessStepRepository projectProcessStepRepository;
  @Mock private ProjectProcessTaskRepository projectProcessTaskRepository;

  @InjectMocks private ProcessPlanService processPlanService;

  @Test
  void generateBuildsPlanFromCatalogs() {
    UUID projectId = UUID.randomUUID();
    UUID candidateId = UUID.randomUUID();
    UUID planId = UUID.randomUUID();
    UUID sitePrepCatalogId = UUID.randomUUID();
    UUID electricalCatalogId = UUID.randomUUID();
    UUID firstStepId = UUID.randomUUID();
    UUID secondStepId = UUID.randomUUID();

    ProjectEntity project = new ProjectEntity();
    project.setId(projectId);
    project.setProjectType(ProjectType.PARTIAL);
    project.setLivingStatus(LivingStatus.BEFORE_MOVE_IN);

    FloorPlanCandidateEntity candidate = new FloorPlanCandidateEntity();
    candidate.setId(candidateId);
    candidate.setProjectId(projectId);
    candidate.setConfidenceScore(BigDecimal.valueOf(90));
    candidate.setSelected(true);

    ProcessCatalogEntity sitePrep = new ProcessCatalogEntity();
    sitePrep.setId(sitePrepCatalogId);
    sitePrep.setStepKey("SITE_PREP");
    sitePrep.setTitle("현장 준비");
    sitePrep.setDescription("현장 준비");
    sitePrep.setSortOrder(10);
    sitePrep.setDefaultDurationDays(2);
    sitePrep.setApplicableProjectTypes(List.of("FULL", "PARTIAL"));
    sitePrep.setApplicableLivingStatuses(List.of("BEFORE_MOVE_IN", "OCCUPIED"));

    ProcessCatalogEntity electrical = new ProcessCatalogEntity();
    electrical.setId(electricalCatalogId);
    electrical.setStepKey("ELECTRICAL");
    electrical.setTitle("전기");
    electrical.setDescription("전기");
    electrical.setSortOrder(30);
    electrical.setDefaultDurationDays(3);
    electrical.setApplicableProjectTypes(List.of("FULL", "PARTIAL"));
    electrical.setApplicableLivingStatuses(List.of("BEFORE_MOVE_IN", "OCCUPIED"));

    ProcessChecklistItemEntity sitePrepTask = new ProcessChecklistItemEntity();
    sitePrepTask.setProcessCatalogId(sitePrepCatalogId);
    sitePrepTask.setTaskGroup(ProcessTaskGroup.PREPARE);
    sitePrepTask.setItemOrder(10);
    sitePrepTask.setTitle("도면 비교");

    ProcessChecklistItemEntity electricalTask = new ProcessChecklistItemEntity();
    electricalTask.setProcessCatalogId(electricalCatalogId);
    electricalTask.setTaskGroup(ProcessTaskGroup.DECISION);
    electricalTask.setItemOrder(20);
    electricalTask.setTitle("콘센트 위치");

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(projectProcessPlanRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
    when(floorPlanCandidateRepository.findByProjectIdAndSelectedTrue(projectId))
        .thenReturn(Optional.of(candidate));
    when(processCatalogRepository.findAllByOrderBySortOrderAsc())
        .thenReturn(List.of(sitePrep, electrical));
    when(projectProcessPlanRepository.save(any()))
        .thenAnswer(
            invocation -> {
              ProjectProcessPlanEntity entity = invocation.getArgument(0);
              entity.setId(planId);
              return entity;
            });
    when(projectProcessStepRepository.save(any()))
        .thenAnswer(
            invocation -> {
              ProjectProcessStepEntity entity = invocation.getArgument(0);
              if ("SITE_PREP".equals(entity.getStepKey())) {
                entity.setId(firstStepId);
              } else {
                entity.setId(UUID.randomUUID());
              }
              return entity;
            });
    when(processChecklistItemRepository.findByProcessCatalogIdOrderByItemOrderAsc(
            sitePrepCatalogId))
        .thenReturn(List.of(sitePrepTask));
    when(processChecklistItemRepository.findByProcessCatalogIdOrderByItemOrderAsc(
            electricalCatalogId))
        .thenReturn(List.of(electricalTask));
    when(projectProcessTaskRepository.saveAll(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(projectProcessStepRepository.findByProcessPlanIdOrderBySortOrderAsc(planId))
        .thenReturn(
            List.of(
                buildStep(
                    planId,
                    sitePrepCatalogId,
                    firstStepId,
                    "SITE_PREP",
                    10,
                    ProcessStepStatus.IN_PROGRESS),
                buildStep(
                    planId,
                    electricalCatalogId,
                    secondStepId,
                    "ELECTRICAL",
                    30,
                    ProcessStepStatus.TODO)));
    when(projectProcessTaskRepository.findByProjectProcessStepIdOrderByItemOrderAsc(firstStepId))
        .thenReturn(List.of(buildTask(firstStepId, true)));
    when(projectProcessTaskRepository.findByProjectProcessStepIdOrderByItemOrderAsc(secondStepId))
        .thenReturn(List.of(buildTask(secondStepId, false)));

    var response = processPlanService.generate(projectId.toString());

    assertThat(response.planId()).isEqualTo(planId.toString());
    assertThat(response.currentStepKey()).isEqualTo("SITE_PREP");
    assertThat(response.steps()).hasSize(2);
    assertThat(response.steps().get(0).status()).isEqualTo("IN_PROGRESS");
  }

  @Test
  void generateFailsWithoutSelectedFloorPlan() {
    UUID projectId = UUID.randomUUID();

    ProjectEntity project = new ProjectEntity();
    project.setId(projectId);
    project.setProjectType(ProjectType.FULL);
    project.setLivingStatus(LivingStatus.BEFORE_MOVE_IN);

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(projectProcessPlanRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
    when(floorPlanCandidateRepository.findByProjectIdAndSelectedTrue(projectId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> processPlanService.generate(projectId.toString()))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("선택된 도면 후보");
  }

  @Test
  void toggleTaskUpdatesCurrentStep() {
    UUID projectId = UUID.randomUUID();
    UUID planId = UUID.randomUUID();
    UUID firstCatalogId = UUID.randomUUID();
    UUID secondCatalogId = UUID.randomUUID();
    UUID firstStepId = UUID.randomUUID();
    UUID secondStepId = UUID.randomUUID();
    UUID taskId = UUID.randomUUID();

    ProjectEntity project = new ProjectEntity();
    project.setId(projectId);
    project.setCurrentProcessStep("SITE_PREP");

    ProjectProcessPlanEntity plan = new ProjectProcessPlanEntity();
    plan.setId(planId);
    plan.setProjectId(projectId);
    plan.setPlanStatus(ProcessPlanStatus.IN_PROGRESS);
    plan.setCurrentStepKey("SITE_PREP");

    ProjectProcessStepEntity firstStep =
        buildStep(
            planId, firstCatalogId, firstStepId, "SITE_PREP", 10, ProcessStepStatus.IN_PROGRESS);
    ProjectProcessStepEntity secondStep =
        buildStep(planId, secondCatalogId, secondStepId, "ELECTRICAL", 30, ProcessStepStatus.TODO);

    ProjectProcessTaskEntity task = buildTask(firstStepId, false);
    task.setId(taskId);

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(projectProcessPlanRepository.findByProjectId(projectId)).thenReturn(Optional.of(plan));
    when(projectProcessTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
    when(projectProcessStepRepository.findById(firstStepId)).thenReturn(Optional.of(firstStep));
    when(projectProcessStepRepository.findByProcessPlanIdOrderBySortOrderAsc(planId))
        .thenReturn(List.of(firstStep, secondStep));
    when(projectProcessTaskRepository.findByProjectProcessStepIdOrderByItemOrderAsc(firstStepId))
        .thenReturn(List.of(task));
    when(projectProcessTaskRepository.findByProjectProcessStepIdOrderByItemOrderAsc(secondStepId))
        .thenReturn(List.of(buildTask(secondStepId, false)));
    when(projectProcessTaskRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(projectProcessStepRepository.saveAll(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(projectProcessPlanRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(projectRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(projectProcessStepRepository.findById(firstStepId)).thenReturn(Optional.of(firstStep));
    when(projectProcessPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

    var response =
        processPlanService.toggleTask(
            projectId.toString(),
            taskId.toString(),
            new ProcessPlanController.ProcessTaskToggleRequest(true));

    assertThat(response.completed()).isTrue();
    assertThat(response.currentStepKey()).isEqualTo("ELECTRICAL");
    assertThat(project.getCurrentProcessStep()).isEqualTo("ELECTRICAL");
  }

  private static ProjectProcessStepEntity buildStep(
      UUID planId,
      UUID catalogId,
      UUID stepId,
      String stepKey,
      int sortOrder,
      ProcessStepStatus status) {
    ProjectProcessStepEntity step = new ProjectProcessStepEntity();
    step.setId(stepId);
    step.setProcessPlanId(planId);
    step.setProcessCatalogId(catalogId);
    step.setStepKey(stepKey);
    step.setTitle(stepKey);
    step.setStatus(status);
    step.setSortOrder(sortOrder);
    step.setDurationDays(2);
    step.setRequired(true);
    step.setDescription(stepKey);
    return step;
  }

  private static ProjectProcessTaskEntity buildTask(UUID stepId, boolean completed) {
    ProjectProcessTaskEntity task = new ProjectProcessTaskEntity();
    task.setProjectProcessStepId(stepId);
    task.setTaskGroup(ProcessTaskGroup.PREPARE);
    task.setItemOrder(10);
    task.setTitle("task");
    task.setDescription("desc");
    task.setCompleted(completed);
    return task;
  }
}
