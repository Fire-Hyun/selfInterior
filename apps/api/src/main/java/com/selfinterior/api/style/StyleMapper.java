package com.selfinterior.api.style;

import com.selfinterior.api.style.StyleController.StyleImageResponse;
import com.selfinterior.api.style.StyleController.StylePresetResponse;
import java.util.List;
import java.util.Map;

public final class StyleMapper {
  private StyleMapper() {}

  public static StylePresetResponse toPreset(StylePresetEntity preset) {
    return new StylePresetResponse(
        preset.getId().toString(),
        preset.getKey(),
        preset.getName(),
        preset.getDescription(),
        preset.getPromptTemplate());
  }

  @SuppressWarnings("unchecked")
  public static StyleImageResponse toImage(
      GeneratedStyleImageEntity image, StylePresetEntity preset) {
    Map<String, Object> metadata = image.getMetadata() == null ? Map.of() : image.getMetadata();
    return new StyleImageResponse(
        image.getId().toString(),
        image.getSpaceType().name(),
        preset == null ? null : preset.getKey(),
        preset == null ? null : preset.getName(),
        image.getPromptText(),
        image.getNegativePromptText(),
        image.getGenerationStatus().name(),
        image.getStorageKey(),
        image.getThumbnailKey(),
        image.getSeed(),
        image.getModelName(),
        image.isLiked(),
        (String) metadata.getOrDefault("difficulty", ""),
        (String) metadata.getOrDefault("budgetImpact", ""),
        (List<String>) metadata.getOrDefault("suggestedProcessSteps", List.of()),
        metadata);
  }
}
