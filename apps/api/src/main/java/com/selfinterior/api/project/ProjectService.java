package com.selfinterior.api.project;

import com.selfinterior.api.common.api.ApiException;
import com.selfinterior.api.common.api.ErrorCode;
import com.selfinterior.api.floorplan.FloorPlanCandidateEntity;
import com.selfinterior.api.floorplan.FloorPlanCandidateRepository;
import com.selfinterior.api.floorplan.FloorPlanSourceEntity;
import com.selfinterior.api.floorplan.FloorPlanSourceRepository;
import com.selfinterior.api.project.ProjectController.CreateProjectRequest;
import com.selfinterior.api.project.ProjectController.CreateProjectResponse;
import com.selfinterior.api.project.ProjectController.ProjectDetailResponse;
import com.selfinterior.api.project.ProjectController.ProjectSummaryResponse;
import com.selfinterior.api.project.ProjectController.UpdateProjectRequest;
import com.selfinterior.api.property.PropertyEntity;
import com.selfinterior.api.property.PropertyRepository;
import com.selfinterior.api.user.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {
  private final ProjectRepository projectRepository;
  private final ProjectMemberRepository projectMemberRepository;
  private final ProjectScopeRepository projectScopeRepository;
  private final PropertyRepository propertyRepository;
  private final FloorPlanCandidateRepository floorPlanCandidateRepository;
  private final FloorPlanSourceRepository floorPlanSourceRepository;
  private final UserRepository userRepository;

  @Value("${app.default-owner-user-id}")
  private UUID defaultOwnerUserId;

  @Transactional
  public CreateProjectResponse create(CreateProjectRequest request) {
    userRepository
        .findById(defaultOwnerUserId)
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.INTERNAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "기본 소유자 계정을 찾을 수 없습니다."));

    ProjectEntity entity = new ProjectEntity();
    entity.setOwnerUserId(defaultOwnerUserId);
    entity.setTitle(request.title());
    entity.setStatus(ProjectStatus.DRAFT);
    entity.setProjectType(request.projectType());
    entity.setLivingStatus(request.livingStatus());
    entity.setBudgetMin(request.budgetMin());
    entity.setBudgetMax(request.budgetMax());
    entity.setCurrency("KRW");
    entity.setOnboardingCompleted(false);
    ProjectEntity saved = projectRepository.save(entity);

    ProjectMemberEntity member = new ProjectMemberEntity();
    member.setProjectId(saved.getId());
    member.setUserId(defaultOwnerUserId);
    member.setMemberRole(MemberRole.OWNER);
    projectMemberRepository.save(member);

    ProjectScopeEntity scope = new ProjectScopeEntity();
    scope.setProjectId(saved.getId());
    scope.setScopeType(request.projectType());
    scope.setSpacesTargeted(List.of());
    scope.setKeepItems(List.of());
    scope.setSelfWorkItems(List.of());
    scope.setDesiredStyleKeywords(List.of());
    projectScopeRepository.save(scope);

    return new CreateProjectResponse(ProjectMapper.toSummary(saved, false));
  }

  public List<ProjectSummaryResponse> list() {
    return projectRepository.findAll().stream()
        .map(
            project ->
                ProjectMapper.toSummary(
                    project, propertyRepository.findByProjectId(project.getId()).isPresent()))
        .toList();
  }

  public ProjectDetailResponse get(String projectId) {
    return toDetail(findProject(projectId));
  }

  @Transactional
  public ProjectDetailResponse update(String projectId, UpdateProjectRequest request) {
    ProjectEntity project = findProject(projectId);
    if (request.title() != null && !request.title().isBlank()) {
      project.setTitle(request.title());
    }
    if (request.budgetMin() != null) {
      project.setBudgetMin(request.budgetMin());
    }
    if (request.budgetMax() != null) {
      project.setBudgetMax(request.budgetMax());
    }

    return toDetail(projectRepository.save(project));
  }

  private ProjectEntity findProject(String projectId) {
    return projectRepository
        .findById(UUID.fromString(projectId))
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
  }

  private ProjectDetailResponse toDetail(ProjectEntity project) {
    PropertyEntity property = propertyRepository.findByProjectId(project.getId()).orElse(null);
    FloorPlanCandidateEntity selectedCandidate =
        floorPlanCandidateRepository.findByProjectIdAndSelectedTrue(project.getId()).orElse(null);
    FloorPlanSourceEntity selectedSource =
        selectedCandidate == null
            ? null
            : floorPlanSourceRepository
                .findById(selectedCandidate.getFloorPlanSourceId())
                .orElse(null);

    return ProjectMapper.toDetail(
        project,
        property,
        selectedCandidate,
        selectedSource,
        floorPlanCandidateRepository.findByProjectId(project.getId()).size());
  }
}
