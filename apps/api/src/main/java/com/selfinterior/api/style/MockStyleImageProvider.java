package com.selfinterior.api.style;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockStyleImageProvider implements StyleImageProvider {
  @Override
  public List<StyleImageResult> generate(StyleGenerationCommand command) {
    List<StyleImageResult> results = new ArrayList<>();
    for (int index = 0; index < 2; index++) {
      String variant = index == 0 ? "A" : "B";
      results.add(
          new StyleImageResult(
              "mock://styles/"
                  + command.stylePresetKey().toLowerCase()
                  + "/"
                  + command.spaceType().name().toLowerCase()
                  + "-"
                  + variant
                  + ".webp",
              "mock://styles/thumbs/"
                  + command.stylePresetKey().toLowerCase()
                  + "/"
                  + command.spaceType().name().toLowerCase()
                  + "-"
                  + variant
                  + ".webp",
              command.stylePresetKey() + "-" + command.spaceType().name() + "-" + variant,
              "mock-style-v1",
              Map.of(
                  "difficulty",
                  index == 0 ? "EASY" : "MEDIUM",
                  "budgetImpact",
                  normalizeBudgetImpact(command.budgetLevel(), index),
                  "suggestedProcessSteps",
                  suggestedSteps(command.spaceType()),
                  "variantLabel",
                  variant)));
    }
    return results;
  }

  private String normalizeBudgetImpact(String budgetLevel, int index) {
    if ("HIGH".equalsIgnoreCase(budgetLevel)) {
      return index == 0 ? "MID" : "HIGH";
    }
    if ("LOW".equalsIgnoreCase(budgetLevel)) {
      return index == 0 ? "LOW" : "MID";
    }
    return index == 0 ? "MID" : "MID_HIGH";
  }

  private List<String> suggestedSteps(com.selfinterior.api.visualqa.SpaceType spaceType) {
    return switch (spaceType) {
      case KITCHEN -> List.of("ELECTRICAL", "SURFACE_FINISH", "FINAL_FIXTURE");
      case BATHROOM -> List.of("ISSUE_DIAGNOSIS", "SURFACE_FINISH", "FINAL_FIXTURE");
      default -> List.of("SITE_PREP", "SURFACE_FINISH", "FINAL_FIXTURE");
    };
  }
}
