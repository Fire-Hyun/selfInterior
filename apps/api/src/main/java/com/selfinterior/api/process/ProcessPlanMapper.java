package com.selfinterior.api.process;

import com.selfinterior.api.process.ProcessPlanController.ProcessPlanResponse;
import com.selfinterior.api.process.ProcessPlanController.ProcessPlanStepDetailResponse;
import com.selfinterior.api.process.ProcessPlanController.ProcessStepSummaryResponse;
import com.selfinterior.api.process.ProcessPlanController.ProcessTaskResponse;
import java.util.List;
import java.util.Map;

public final class ProcessPlanMapper {
  private ProcessPlanMapper() {}

  public static ProcessPlanResponse toPlanResponse(
      ProjectProcessPlanEntity plan,
      List<ProjectProcessStepEntity> steps,
      Map<String, TaskCount> taskCountByStepKey) {
    int totalTaskCount =
        taskCountByStepKey.values().stream().mapToInt(TaskCount::totalTaskCount).sum();
    int completedTaskCount =
        taskCountByStepKey.values().stream().mapToInt(TaskCount::completedTaskCount).sum();

    int progressPercent =
        totalTaskCount == 0 ? 0 : (int) Math.round((completedTaskCount * 100.0) / totalTaskCount);

    return new ProcessPlanResponse(
        plan.getId().toString(),
        plan.getPlanStatus().name(),
        plan.getCurrentStepKey(),
        progressPercent,
        plan.getCreatedAt(),
        steps.stream()
            .map(
                step -> {
                  TaskCount taskCount =
                      taskCountByStepKey.getOrDefault(step.getStepKey(), TaskCount.empty());
                  return toStepSummary(
                      step, taskCount.completedTaskCount(), taskCount.totalTaskCount());
                })
            .toList());
  }

  public static ProcessPlanStepDetailResponse toStepDetailResponse(
      ProjectProcessPlanEntity plan,
      ProjectProcessStepEntity step,
      ProcessGuideEntity guide,
      List<ProjectProcessTaskEntity> tasks) {
    long completedTaskCount = tasks.stream().filter(ProjectProcessTaskEntity::isCompleted).count();

    return new ProcessPlanStepDetailResponse(
        plan.getId().toString(),
        toStepSummary(step, (int) completedTaskCount, tasks.size()),
        guide.getPurposeText(),
        guide.getStartCheckIntro(),
        guide.getDecisionPoints(),
        guide.getSelfWorkText(),
        guide.getExpertRequiredText(),
        guide.getMistakesText(),
        guide.getNextStepChecks(),
        tasks.stream().map(ProcessPlanMapper::toTaskResponse).toList());
  }

  public static ProcessTaskResponse toTaskResponse(ProjectProcessTaskEntity task) {
    return new ProcessTaskResponse(
        task.getId().toString(),
        task.getTaskGroup().name(),
        task.getItemOrder(),
        task.getTitle(),
        task.getDescription(),
        task.isCompleted());
  }

  public static ProcessStepSummaryResponse toStepSummary(
      ProjectProcessStepEntity step, int completedTaskCount, int totalTaskCount) {
    return new ProcessStepSummaryResponse(
        step.getStepKey(),
        step.getTitle(),
        step.getStatus().name(),
        step.getSortOrder(),
        step.getDurationDays(),
        step.isRequired(),
        completedTaskCount,
        totalTaskCount);
  }

  public record TaskCount(int completedTaskCount, int totalTaskCount) {
    static TaskCount empty() {
      return new TaskCount(0, 0);
    }
  }
}
