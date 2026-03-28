package com.selfinterior.api.style;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.selfinterior.api.project.ProjectEntity;
import com.selfinterior.api.project.ProjectRepository;
import com.selfinterior.api.project.ProjectScopeRepository;
import com.selfinterior.api.project.ProjectType;
import com.selfinterior.api.visualqa.SpaceType;
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
class StyleServiceTest {
  @Mock private StylePresetRepository stylePresetRepository;
  @Mock private ProjectStyleSelectionRepository projectStyleSelectionRepository;
  @Mock private GeneratedStyleImageRepository generatedStyleImageRepository;
  @Mock private ProjectRepository projectRepository;
  @Mock private ProjectScopeRepository projectScopeRepository;
  @Mock private StyleImageProvider styleImageProvider;

  @InjectMocks private StyleService styleService;

  @Test
  void generateStoresPromptAndMetadata() {
    UUID projectId = UUID.randomUUID();
    UUID presetId = UUID.randomUUID();
    UUID imageId = UUID.randomUUID();

    ProjectEntity project = new ProjectEntity();
    project.setId(projectId);
    project.setTitle("스타일 프로젝트");
    project.setProjectType(ProjectType.PARTIAL);

    StylePresetEntity preset = new StylePresetEntity();
    preset.setId(presetId);
    preset.setKey("WHITE_MINIMAL");
    preset.setName("화이트 미니멀");
    preset.setPromptTemplate("white interior for {{spaceType}}, keep {{keepItems}}");
    preset.setActive(true);

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(projectScopeRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
    when(stylePresetRepository.findByKeyAndActiveTrue("WHITE_MINIMAL"))
        .thenReturn(Optional.of(preset));
    when(projectStyleSelectionRepository.findByProjectIdAndSpaceType(
            projectId, SpaceType.LIVING_ROOM))
        .thenReturn(List.of());
    when(styleImageProvider.generate(any()))
        .thenReturn(
            List.of(
                new StyleImageProvider.StyleImageResult(
                    "mock://styles/a.webp",
                    "mock://styles/thumbs/a.webp",
                    "seed-a",
                    "mock-style-v1",
                    Map.of(
                        "difficulty", "EASY",
                        "budgetImpact", "MID",
                        "suggestedProcessSteps", List.of("SURFACE_FINISH")))));
    when(generatedStyleImageRepository.save(any()))
        .thenAnswer(
            invocation -> {
              GeneratedStyleImageEntity entity = invocation.getArgument(0);
              entity.setId(imageId);
              return entity;
            });
    when(projectStyleSelectionRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        styleService.generate(
            projectId.toString(),
            new StyleController.GenerateStyleImagesRequest(
                List.of(SpaceType.LIVING_ROOM),
                "WHITE_MINIMAL",
                "MID",
                List.of("WINDOWS"),
                "실거주 무드"));

    ArgumentCaptor<GeneratedStyleImageEntity> imageCaptor =
        ArgumentCaptor.forClass(GeneratedStyleImageEntity.class);
    org.mockito.Mockito.verify(generatedStyleImageRepository).save(imageCaptor.capture());

    assertThat(response.imageCount()).isEqualTo(1);
    assertThat(response.imageIds()).containsExactly(imageId.toString());
    assertThat(imageCaptor.getValue().getPromptText()).contains("living_room");
    assertThat(imageCaptor.getValue().getMetadata()).containsEntry("difficulty", "EASY");
    assertThat(imageCaptor.getValue().getModelName()).isEqualTo("mock-style-v1");
  }

  @Test
  void likeMarksTargetImageAndSelection() {
    UUID projectId = UUID.randomUUID();
    UUID presetId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();
    UUID oldId = UUID.randomUUID();

    ProjectEntity project = new ProjectEntity();
    project.setId(projectId);

    StylePresetEntity preset = new StylePresetEntity();
    preset.setId(presetId);
    preset.setKey("WARM_NATURAL");
    preset.setName("웜 내추럴");

    GeneratedStyleImageEntity target = new GeneratedStyleImageEntity();
    target.setId(targetId);
    target.setProjectId(projectId);
    target.setStylePresetId(presetId);
    target.setSpaceType(SpaceType.KITCHEN);
    target.setGenerationStatus(StyleGenerationStatus.SUCCESS);

    GeneratedStyleImageEntity old = new GeneratedStyleImageEntity();
    old.setId(oldId);
    old.setProjectId(projectId);
    old.setStylePresetId(UUID.randomUUID());
    old.setSpaceType(SpaceType.KITCHEN);
    old.setGenerationStatus(StyleGenerationStatus.SUCCESS);
    old.setLiked(true);

    ProjectStyleSelectionEntity selection = new ProjectStyleSelectionEntity();
    selection.setProjectId(projectId);
    selection.setStylePresetId(old.getStylePresetId());
    selection.setSpaceType(SpaceType.KITCHEN);
    selection.setSelected(true);
    selection.setPriority(1);

    when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    when(generatedStyleImageRepository.findById(targetId)).thenReturn(Optional.of(target));
    when(generatedStyleImageRepository.findByProjectIdAndSpaceType(projectId, SpaceType.KITCHEN))
        .thenReturn(List.of(target, old));
    when(projectStyleSelectionRepository.findByProjectIdAndSpaceType(projectId, SpaceType.KITCHEN))
        .thenReturn(List.of(selection));
    when(generatedStyleImageRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(projectStyleSelectionRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(stylePresetRepository.findById(presetId)).thenReturn(Optional.of(preset));

    var response = styleService.like(projectId.toString(), targetId.toString());

    assertThat(response.imageId()).isEqualTo(targetId.toString());
    assertThat(response.liked()).isTrue();
    assertThat(response.selectedStylePresetKey()).isEqualTo("WARM_NATURAL");
    assertThat(selection.isSelected()).isFalse();
    assertThat(target.isLiked()).isTrue();
    assertThat(old.isLiked()).isFalse();
  }
}
