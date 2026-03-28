package com.selfinterior.api.style;

import com.selfinterior.api.visualqa.SpaceType;
import java.util.List;
import java.util.Map;

public interface StyleImageProvider {
  List<StyleImageResult> generate(StyleGenerationCommand command);

  record StyleGenerationCommand(
      String projectTitle,
      SpaceType spaceType,
      String stylePresetKey,
      String stylePresetName,
      String promptText,
      String negativePromptText,
      String budgetLevel,
      List<String> keepItems,
      String extraPrompt) {}

  record StyleImageResult(
      String storageKey,
      String thumbnailKey,
      String seed,
      String modelName,
      Map<String, Object> metadata) {}
}
