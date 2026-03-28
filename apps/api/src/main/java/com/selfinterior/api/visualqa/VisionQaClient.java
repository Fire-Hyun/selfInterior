package com.selfinterior.api.visualqa;

import java.util.List;

public interface VisionQaClient {
  VisionQaResult analyze(
      String questionText, String processStepKey, SpaceType spaceType, List<String> imagePaths);

  record VisionQaResult(
      RiskLevel riskLevel,
      String observedText,
      String possibleCausesText,
      String nextChecksText,
      String proceedRecommendationText,
      boolean expertRequired,
      double confidenceScore) {}
}
