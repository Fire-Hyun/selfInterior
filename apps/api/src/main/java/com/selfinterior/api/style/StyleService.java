package com.selfinterior.api.style;

import com.selfinterior.api.common.api.ApiException;
import com.selfinterior.api.common.api.ErrorCode;
import com.selfinterior.api.project.ProjectEntity;
import com.selfinterior.api.project.ProjectRepository;
import com.selfinterior.api.project.ProjectScopeEntity;
import com.selfinterior.api.project.ProjectScopeRepository;
import com.selfinterior.api.style.StyleController.GenerateStyleImagesRequest;
import com.selfinterior.api.style.StyleController.GenerateStyleImagesResponse;
import com.selfinterior.api.style.StyleController.LikeStyleImageResponse;
import com.selfinterior.api.style.StyleController.StyleImageResponse;
import com.selfinterior.api.style.StyleController.StylePresetResponse;
import com.selfinterior.api.visualqa.SpaceType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StyleService {
  private final StylePresetRepository stylePresetRepository;
  private final ProjectStyleSelectionRepository projectStyleSelectionRepository;
  private final GeneratedStyleImageRepository generatedStyleImageRepository;
  private final ProjectRepository projectRepository;
  private final ProjectScopeRepository projectScopeRepository;
  private final StyleImageProvider styleImageProvider;

  public List<StylePresetResponse> listPresets() {
    return stylePresetRepository.findByActiveTrueOrderByNameAsc().stream()
        .map(StyleMapper::toPreset)
        .toList();
  }

  @Transactional
  public GenerateStyleImagesResponse generate(
      String projectId, GenerateStyleImagesRequest request) {
    ProjectEntity project = findProject(projectId);
    ProjectScopeEntity scope = projectScopeRepository.findByProjectId(project.getId()).orElse(null);
    StylePresetEntity preset = findPreset(request.stylePresetKey());

    List<String> createdImageIds = new ArrayList<>();
    int priority = 1;

    for (SpaceType spaceType : request.spaceTypes()) {
      String promptText = buildPrompt(project, scope, preset, spaceType, request);
      String negativePrompt = "cluttered layout, heavy luxury, unrealistic lighting";

      List<StyleImageProvider.StyleImageResult> generated =
          styleImageProvider.generate(
              new StyleImageProvider.StyleGenerationCommand(
                  project.getTitle(),
                  spaceType,
                  preset.getKey(),
                  preset.getName(),
                  promptText,
                  negativePrompt,
                  request.budgetLevel(),
                  mergeKeepItems(scope, request.keepItems()),
                  request.extraPrompt()));

      for (StyleImageProvider.StyleImageResult result : generated) {
        GeneratedStyleImageEntity image = new GeneratedStyleImageEntity();
        image.setProjectId(project.getId());
        image.setStylePresetId(preset.getId());
        image.setSpaceType(spaceType);
        image.setPromptText(promptText);
        image.setNegativePromptText(negativePrompt);
        image.setGenerationStatus(StyleGenerationStatus.SUCCESS);
        image.setStorageKey(result.storageKey());
        image.setThumbnailKey(result.thumbnailKey());
        image.setSeed(result.seed());
        image.setModelName(result.modelName());
        image.setMetadata(result.metadata());
        image.setLiked(false);
        GeneratedStyleImageEntity saved = generatedStyleImageRepository.save(image);
        createdImageIds.add(saved.getId().toString());
      }

      if (projectStyleSelectionRepository
          .findByProjectIdAndSpaceType(project.getId(), spaceType)
          .isEmpty()) {
        ProjectStyleSelectionEntity selection = new ProjectStyleSelectionEntity();
        selection.setProjectId(project.getId());
        selection.setStylePresetId(preset.getId());
        selection.setSpaceType(spaceType);
        selection.setPriority(priority++);
        selection.setSelected(false);
        projectStyleSelectionRepository.save(selection);
      }
    }

    return new GenerateStyleImagesResponse(createdImageIds.size(), createdImageIds);
  }

  public List<StyleImageResponse> listImages(String projectId) {
    ProjectEntity project = findProject(projectId);
    Map<UUID, StylePresetEntity> presetsById =
        stylePresetRepository.findAll().stream()
            .collect(java.util.stream.Collectors.toMap(StylePresetEntity::getId, preset -> preset));

    return generatedStyleImageRepository
        .findByProjectIdOrderByCreatedAtDesc(project.getId())
        .stream()
        .map(image -> StyleMapper.toImage(image, presetsById.get(image.getStylePresetId())))
        .toList();
  }

  @Transactional
  public LikeStyleImageResponse like(String projectId, String imageId) {
    ProjectEntity project = findProject(projectId);
    GeneratedStyleImageEntity target =
        generatedStyleImageRepository
            .findById(UUID.fromString(imageId))
            .filter(image -> image.getProjectId().equals(project.getId()))
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.STYLE_IMAGE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "스타일 이미지를 찾을 수 없습니다."));

    List<GeneratedStyleImageEntity> sameSpaceImages =
        generatedStyleImageRepository.findByProjectIdAndSpaceType(
            project.getId(), target.getSpaceType());
    for (GeneratedStyleImageEntity image : sameSpaceImages) {
      image.setLiked(image.getId().equals(target.getId()));
      generatedStyleImageRepository.save(image);
    }

    List<ProjectStyleSelectionEntity> selections =
        projectStyleSelectionRepository.findByProjectIdAndSpaceType(
            project.getId(), target.getSpaceType());
    for (ProjectStyleSelectionEntity selection : selections) {
      boolean selected = selection.getStylePresetId().equals(target.getStylePresetId());
      selection.setSelected(selected);
      projectStyleSelectionRepository.save(selection);
    }

    if (selections.stream()
        .noneMatch(selection -> selection.getStylePresetId().equals(target.getStylePresetId()))) {
      ProjectStyleSelectionEntity selection = new ProjectStyleSelectionEntity();
      selection.setProjectId(project.getId());
      selection.setStylePresetId(target.getStylePresetId());
      selection.setSpaceType(target.getSpaceType());
      selection.setPriority(selections.size() + 1);
      selection.setSelected(true);
      projectStyleSelectionRepository.save(selection);
    }

    StylePresetEntity preset =
        target.getStylePresetId() == null
            ? null
            : stylePresetRepository.findById(target.getStylePresetId()).orElse(null);
    return new LikeStyleImageResponse(
        target.getId().toString(),
        true,
        preset == null ? null : preset.getKey(),
        target.getSpaceType().name());
  }

  private ProjectEntity findProject(String projectId) {
    return projectRepository
        .findById(UUID.fromString(projectId))
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
  }

  private StylePresetEntity findPreset(String stylePresetKey) {
    return stylePresetRepository
        .findByKeyAndActiveTrue(stylePresetKey)
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.STYLE_PRESET_NOT_FOUND,
                    HttpStatus.NOT_FOUND,
                    "스타일 preset을 찾을 수 없습니다."));
  }

  private String buildPrompt(
      ProjectEntity project,
      ProjectScopeEntity scope,
      StylePresetEntity preset,
      SpaceType spaceType,
      GenerateStyleImagesRequest request) {
    String keepItems = String.join(", ", mergeKeepItems(scope, request.keepItems()));
    String prompt =
        preset
            .getPromptTemplate()
            .replace("{{spaceType}}", spaceType.name().toLowerCase())
            .replace(
                "{{keepItems}}", keepItems.isBlank() ? "existing windows and fixtures" : keepItems);

    String extra = request.extraPrompt() == null ? "" : request.extraPrompt().trim();
    return prompt
        + ", project type "
        + project.getProjectType().name()
        + ", budget level "
        + request.budgetLevel()
        + (extra.isBlank() ? "" : ", " + extra);
  }

  private List<String> mergeKeepItems(ProjectScopeEntity scope, List<String> requestKeepItems) {
    LinkedHashSet<String> merged = new LinkedHashSet<>();
    if (scope != null && scope.getKeepItems() != null) {
      merged.addAll(scope.getKeepItems());
    }
    if (requestKeepItems != null) {
      merged.addAll(requestKeepItems);
    }
    return List.copyOf(merged);
  }
}
