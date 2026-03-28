package com.selfinterior.api.process;

import com.selfinterior.api.common.api.ApiException;
import com.selfinterior.api.common.api.ErrorCode;
import com.selfinterior.api.floorplan.FloorPlanCandidateEntity;
import com.selfinterior.api.floorplan.FloorPlanCandidateRepository;
import com.selfinterior.api.process.ProcessPlanController.ProcessPlanResponse;
import com.selfinterior.api.process.ProcessPlanController.ProcessPlanStepDetailResponse;
import com.selfinterior.api.process.ProcessPlanController.ProcessTaskToggleRequest;
import com.selfinterior.api.process.ProcessPlanController.ProcessTaskToggleResponse;
import com.selfinterior.api.process.ProcessPlanMapper.TaskCount;
import com.selfinterior.api.project.ProjectEntity;
import com.selfinterior.api.project.ProjectRepository;
import com.selfinterior.api.project.ProjectType;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcessPlanService {
  private final ProjectRepository projectRepository;
  private final FloorPlanCandidateRepository floorPlanCandidateRepository;
  private final ProcessCatalogRepository processCatalogRepository;
  private final ProcessGuideRepository processGuideRepository;
  private final ProcessChecklistItemRepository processChecklistItemRepository;
  private final ProjectProcessPlanRepository projectProcessPlanRepository;
  private final ProjectProcessStepRepository projectProcessStepRepository;
  private final ProjectProcessTaskRepository projectProcessTaskRepository;

  @Transactional
  public ProcessPlanResponse generate(String projectId) {
    ProjectEntity project = findProject(projectId);
    ProjectProcessPlanEntity existingPlan =
        projectProcessPlanRepository.findByProjectId(project.getId()).orElse(null);
    if (existingPlan != null) {
      return toPlanResponse(existingPlan);
    }

    FloorPlanCandidateEntity selectedCandidate =
        floorPlanCandidateRepository.findByProjectIdAndSelectedTrue(project.getId()).orElse(null);
    if (selectedCandidate == null) {
      throw new ApiException(
          ErrorCode.PROCESS_PLAN_GENERATION_BLOCKED,
          HttpStatus.BAD_REQUEST,
          "선택된 도면 후보가 있어야 공정 플랜을 생성할 수 있습니다.");
    }

    List<ProcessCatalogEntity> applicableCatalogs = selectCatalogs(project);
    if (applicableCatalogs.isEmpty()) {
      throw new ApiException(
          ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, "공정 카탈로그가 준비되지 않았습니다.");
    }

    ProjectProcessPlanEntity plan = new ProjectProcessPlanEntity();
    plan.setProjectId(project.getId());
    plan.setPlanStatus(ProcessPlanStatus.IN_PROGRESS);
    plan.setGeneratedFromFloorPlanId(selectedCandidate.getId());
    plan.setCurrentStepKey(applicableCatalogs.get(0).getStepKey());
    plan.setGeneratedSummary(
        Map.of(
            "projectType", project.getProjectType().name(),
            "livingStatus", project.getLivingStatus().name(),
            "generatedAt", OffsetDateTime.now().toString()));
    ProjectProcessPlanEntity savedPlan = projectProcessPlanRepository.save(plan);

    List<ProjectProcessStepEntity> savedSteps = new ArrayList<>();
    for (int i = 0; i < applicableCatalogs.size(); i++) {
      ProcessCatalogEntity catalog = applicableCatalogs.get(i);

      ProjectProcessStepEntity step = new ProjectProcessStepEntity();
      step.setProcessPlanId(savedPlan.getId());
      step.setProcessCatalogId(catalog.getId());
      step.setStepKey(catalog.getStepKey());
      step.setTitle(catalog.getTitle());
      step.setStatus(i == 0 ? ProcessStepStatus.IN_PROGRESS : ProcessStepStatus.TODO);
      step.setSortOrder(catalog.getSortOrder());
      step.setDurationDays(catalog.getDefaultDurationDays());
      step.setRequired(true);
      step.setDescription(catalog.getDescription());
      ProjectProcessStepEntity savedStep = projectProcessStepRepository.save(step);
      savedSteps.add(savedStep);

      createTasksForStep(project, catalog, savedStep);
    }

    project.setCurrentProcessStep(savedPlan.getCurrentStepKey());
    projectRepository.save(project);

    return toPlanResponse(savedPlan);
  }

  public ProcessPlanResponse get(String projectId) {
    ProjectEntity project = findProject(projectId);
    ProjectProcessPlanEntity plan =
        projectProcessPlanRepository
            .findByProjectId(project.getId())
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.PROCESS_PLAN_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "공정 플랜을 찾을 수 없습니다."));
    return toPlanResponse(plan);
  }

  public ProcessPlanStepDetailResponse getStep(String projectId, String stepKey) {
    ProjectEntity project = findProject(projectId);
    ProjectProcessPlanEntity plan =
        projectProcessPlanRepository
            .findByProjectId(project.getId())
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.PROCESS_PLAN_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "공정 플랜을 찾을 수 없습니다."));
    ProjectProcessStepEntity step =
        projectProcessStepRepository
            .findByProcessPlanIdAndStepKey(plan.getId(), stepKey)
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.PROCESS_PLAN_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "공정 단계를 찾을 수 없습니다."));
    ProcessGuideEntity guide =
        processGuideRepository
            .findByProcessCatalogId(step.getProcessCatalogId())
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.INTERNAL_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "공정 가이드를 찾을 수 없습니다."));
    List<ProjectProcessTaskEntity> tasks =
        projectProcessTaskRepository.findByProjectProcessStepIdOrderByItemOrderAsc(step.getId());

    return ProcessPlanMapper.toStepDetailResponse(plan, step, guide, tasks);
  }

  @Transactional
  public ProcessTaskToggleResponse toggleTask(
      String projectId, String taskId, ProcessTaskToggleRequest request) {
    ProjectEntity project = findProject(projectId);
    ProjectProcessPlanEntity plan =
        projectProcessPlanRepository
            .findByProjectId(project.getId())
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.PROCESS_PLAN_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "공정 플랜을 찾을 수 없습니다."));
    ProjectProcessTaskEntity task =
        projectProcessTaskRepository
            .findById(UUID.fromString(taskId))
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.PROCESS_TASK_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "공정 태스크를 찾을 수 없습니다."));

    ProjectProcessStepEntity step =
        projectProcessStepRepository
            .findById(task.getProjectProcessStepId())
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.PROCESS_PLAN_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "공정 단계를 찾을 수 없습니다."));
    if (!step.getProcessPlanId().equals(plan.getId())) {
      throw new ApiException(
          ErrorCode.PROCESS_TASK_NOT_FOUND, HttpStatus.NOT_FOUND, "공정 태스크를 찾을 수 없습니다.");
    }

    task.setCompleted(request.completed());
    task.setCompletedAt(request.completed() ? OffsetDateTime.now() : null);
    projectProcessTaskRepository.save(task);

    recalculatePlan(project, plan);

    ProjectProcessStepEntity refreshedStep =
        projectProcessStepRepository.findById(step.getId()).orElseThrow();
    ProjectProcessPlanEntity refreshedPlan =
        projectProcessPlanRepository.findById(plan.getId()).orElseThrow();

    return new ProcessTaskToggleResponse(
        task.getId().toString(),
        task.isCompleted(),
        refreshedStep.getStatus().name(),
        refreshedPlan.getCurrentStepKey());
  }

  private ProcessPlanResponse toPlanResponse(ProjectProcessPlanEntity plan) {
    List<ProjectProcessStepEntity> steps =
        projectProcessStepRepository.findByProcessPlanIdOrderBySortOrderAsc(plan.getId());
    Map<String, TaskCount> taskCountByStepKey = new LinkedHashMap<>();
    for (ProjectProcessStepEntity step : steps) {
      List<ProjectProcessTaskEntity> tasks =
          projectProcessTaskRepository.findByProjectProcessStepIdOrderByItemOrderAsc(step.getId());
      int completedCount =
          (int) tasks.stream().filter(ProjectProcessTaskEntity::isCompleted).count();
      taskCountByStepKey.put(step.getStepKey(), new TaskCount(completedCount, tasks.size()));
    }
    return ProcessPlanMapper.toPlanResponse(plan, steps, taskCountByStepKey);
  }

  private ProjectEntity findProject(String projectId) {
    return projectRepository
        .findById(UUID.fromString(projectId))
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
  }

  private List<ProcessCatalogEntity> selectCatalogs(ProjectEntity project) {
    List<ProcessCatalogEntity> catalogs = processCatalogRepository.findAllByOrderBySortOrderAsc();
    return catalogs.stream()
        .filter(
            catalog ->
                catalog.getApplicableProjectTypes().contains(project.getProjectType().name()))
        .filter(
            catalog ->
                catalog.getApplicableLivingStatuses().contains(project.getLivingStatus().name()))
        .toList();
  }

  private void createTasksForStep(
      ProjectEntity project, ProcessCatalogEntity catalog, ProjectProcessStepEntity savedStep) {
    List<ProcessChecklistItemEntity> templates =
        processChecklistItemRepository.findByProcessCatalogIdOrderByItemOrderAsc(catalog.getId());
    List<ProjectProcessTaskEntity> tasks = new ArrayList<>();

    for (ProcessChecklistItemEntity template : templates) {
      ProjectProcessTaskEntity task = new ProjectProcessTaskEntity();
      task.setProjectProcessStepId(savedStep.getId());
      task.setTaskGroup(template.getTaskGroup());
      task.setItemOrder(template.getItemOrder());
      task.setTitle(template.getTitle());
      task.setDescription(template.getDescription());
      task.setCompleted(false);
      tasks.add(task);
    }

    if ("SITE_PREP".equals(catalog.getStepKey())
        && project.getProjectType() != ProjectType.ISSUE_CHECK
        && "OCCUPIED".equals(project.getLivingStatus().name())) {
      ProjectProcessTaskEntity occupiedTask = new ProjectProcessTaskEntity();
      occupiedTask.setProjectProcessStepId(savedStep.getId());
      occupiedTask.setTaskGroup(ProcessTaskGroup.PREPARE);
      occupiedTask.setItemOrder(15);
      occupiedTask.setTitle("거주 중 공사 동선 분리");
      occupiedTask.setDescription("가림막, 먼지 차단, 공사 시간대 분리를 먼저 정리한다.");
      occupiedTask.setCompleted(false);
      tasks.add(occupiedTask);
    }

    projectProcessTaskRepository.saveAll(tasks);
  }

  private void recalculatePlan(ProjectEntity project, ProjectProcessPlanEntity plan) {
    List<ProjectProcessStepEntity> steps =
        projectProcessStepRepository.findByProcessPlanIdOrderBySortOrderAsc(plan.getId());
    boolean allDone = true;
    String nextCurrentStepKey = null;

    for (ProjectProcessStepEntity step : steps) {
      List<ProjectProcessTaskEntity> tasks =
          projectProcessTaskRepository.findByProjectProcessStepIdOrderByItemOrderAsc(step.getId());
      long completedCount = tasks.stream().filter(ProjectProcessTaskEntity::isCompleted).count();
      ProcessStepStatus newStatus;
      if (!tasks.isEmpty() && completedCount == tasks.size()) {
        newStatus = ProcessStepStatus.DONE;
      } else if (nextCurrentStepKey == null) {
        newStatus = ProcessStepStatus.IN_PROGRESS;
        nextCurrentStepKey = step.getStepKey();
        allDone = false;
      } else {
        newStatus = completedCount > 0 ? ProcessStepStatus.IN_PROGRESS : ProcessStepStatus.TODO;
        if (newStatus != ProcessStepStatus.DONE) {
          allDone = false;
        }
      }
      step.setStatus(newStatus);
    }

    if (allDone) {
      plan.setPlanStatus(ProcessPlanStatus.COMPLETED);
      nextCurrentStepKey = steps.isEmpty() ? null : steps.get(steps.size() - 1).getStepKey();
    } else {
      plan.setPlanStatus(ProcessPlanStatus.IN_PROGRESS);
    }

    plan.setCurrentStepKey(nextCurrentStepKey);
    project.setCurrentProcessStep(nextCurrentStepKey);

    projectProcessStepRepository.saveAll(steps);
    projectProcessPlanRepository.save(plan);
    projectRepository.save(project);
  }
}
