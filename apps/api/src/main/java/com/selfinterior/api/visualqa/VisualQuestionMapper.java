package com.selfinterior.api.visualqa;

import com.selfinterior.api.visualqa.VisualQuestionController.AnswerResponse;
import com.selfinterior.api.visualqa.VisualQuestionController.GuideLinkResponse;
import com.selfinterior.api.visualqa.VisualQuestionController.ImageResponse;
import com.selfinterior.api.visualqa.VisualQuestionController.QuestionResponse;
import com.selfinterior.api.visualqa.VisualQuestionController.VisualQuestionDetailResponse;
import com.selfinterior.api.visualqa.VisualQuestionController.VisualQuestionSummaryResponse;
import java.util.List;

public final class VisualQuestionMapper {
  private VisualQuestionMapper() {}

  public static VisualQuestionSummaryResponse toSummary(
      VisualQuestionEntity question, VisualAnswerEntity answer) {
    return new VisualQuestionSummaryResponse(
        question.getId().toString(),
        question.getQuestionText(),
        question.getProcessStepKey(),
        question.getSpaceType().name(),
        question.getStatus().name(),
        answer == null ? null : answer.getRiskLevel().name(),
        answer != null && answer.isExpertRequired(),
        answer == null ? null : answer.getObservedText(),
        question.getCreatedAt());
  }

  public static VisualQuestionDetailResponse toDetail(
      VisualQuestionEntity question,
      VisualAnswerEntity answer,
      List<VisualQuestionImageEntity> images,
      List<GuideLinkResponse> guideLinks) {
    return new VisualQuestionDetailResponse(
        new QuestionResponse(
            question.getId().toString(),
            question.getQuestionText(),
            question.getProcessStepKey(),
            question.getSpaceType().name(),
            question.getStatus().name(),
            question.getCreatedAt()),
        new AnswerResponse(
            answer.getRiskLevel().name(),
            answer.getObservedText(),
            answer.getPossibleCausesText(),
            answer.getNextChecksText(),
            answer.getProceedRecommendationText(),
            answer.isExpertRequired(),
            answer.getConfidenceScore().doubleValue()),
        images.stream().map(VisualQuestionMapper::toImageResponse).toList(),
        guideLinks);
  }

  public static ImageResponse toImageResponse(VisualQuestionImageEntity image) {
    return new ImageResponse(
        image.getId().toString(),
        image.getFileName(),
        image.getContentType(),
        image.getStoragePath());
  }
}
