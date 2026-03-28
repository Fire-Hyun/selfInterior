package com.selfinterior.api.style;

import com.selfinterior.api.common.api.ApiResponse;
import com.selfinterior.api.visualqa.SpaceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class StyleController {
  private final StyleService styleService;

  @GetMapping("/api/v1/style-presets")
  public ApiResponse<StylePresetListResponse> listPresets() {
    return ApiResponse.ok(new StylePresetListResponse(styleService.listPresets()));
  }

  @PostMapping("/api/v1/projects/{projectId}/styles/generate")
  public ApiResponse<GenerateStyleImagesResponse> generate(
      @PathVariable String projectId, @Valid @RequestBody GenerateStyleImagesRequest request) {
    return ApiResponse.ok(styleService.generate(projectId, request));
  }

  @GetMapping("/api/v1/projects/{projectId}/styles/images")
  public ApiResponse<StyleImageListResponse> listImages(@PathVariable String projectId) {
    return ApiResponse.ok(new StyleImageListResponse(styleService.listImages(projectId)));
  }

  @PostMapping("/api/v1/projects/{projectId}/styles/images/{imageId}/like")
  public ApiResponse<LikeStyleImageResponse> likeImage(
      @PathVariable String projectId, @PathVariable String imageId) {
    return ApiResponse.ok(styleService.like(projectId, imageId));
  }

  public record StylePresetListResponse(List<StylePresetResponse> presets) {}

  public record StylePresetResponse(
      String id, String key, String name, String description, String promptTemplate) {}

  public record GenerateStyleImagesRequest(
      @NotEmpty List<SpaceType> spaceTypes,
      @NotBlank String stylePresetKey,
      @NotBlank String budgetLevel,
      List<String> keepItems,
      String extraPrompt) {}

  public record GenerateStyleImagesResponse(int imageCount, List<String> imageIds) {}

  public record StyleImageListResponse(List<StyleImageResponse> images) {}

  public record StyleImageResponse(
      String id,
      String spaceType,
      String stylePresetKey,
      String stylePresetName,
      String promptText,
      String negativePromptText,
      String generationStatus,
      String storageKey,
      String thumbnailKey,
      String seed,
      String modelName,
      boolean liked,
      String difficulty,
      String budgetImpact,
      List<String> suggestedProcessSteps,
      Map<String, Object> metadata) {}

  public record LikeStyleImageResponse(
      String imageId, boolean liked, String selectedStylePresetKey, String spaceType) {}
}
