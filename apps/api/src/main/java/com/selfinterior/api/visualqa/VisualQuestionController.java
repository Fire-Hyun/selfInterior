package com.selfinterior.api.visualqa;

import com.selfinterior.api.common.api.ApiResponse;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/visual-questions")
@RequiredArgsConstructor
public class VisualQuestionController {
  private final VisualQuestionService visualQuestionService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<CreateVisualQuestionResponse> create(
      @PathVariable String projectId,
      @RequestParam String questionText,
      @RequestParam(required = false) String processStepKey,
      @RequestParam SpaceType spaceType,
      @RequestPart(name = "files", required = false) List<MultipartFile> files) {
    return ApiResponse.ok(
        visualQuestionService.create(projectId, questionText, processStepKey, spaceType, files));
  }

  @GetMapping
  public ApiResponse<VisualQuestionListResponse> list(@PathVariable String projectId) {
    return ApiResponse.ok(visualQuestionService.list(projectId));
  }

  @GetMapping("/{questionId}")
  public ApiResponse<VisualQuestionDetailResponse> get(
      @PathVariable String projectId, @PathVariable String questionId) {
    return ApiResponse.ok(visualQuestionService.get(projectId, questionId));
  }

  public record CreateVisualQuestionResponse(String questionId, String status) {}

  public record VisualQuestionListResponse(List<VisualQuestionSummaryResponse> questions) {}

  public record VisualQuestionSummaryResponse(
      String id,
      String questionText,
      String processStepKey,
      String spaceType,
      String status,
      String riskLevel,
      boolean expertRequired,
      String observedText,
      OffsetDateTime createdAt) {}

  public record VisualQuestionDetailResponse(
      QuestionResponse question,
      AnswerResponse answer,
      List<ImageResponse> images,
      List<GuideLinkResponse> relatedGuideLinks) {}

  public record QuestionResponse(
      String id,
      String questionText,
      String processStepKey,
      String spaceType,
      String status,
      OffsetDateTime createdAt) {}

  public record AnswerResponse(
      String riskLevel,
      String observedText,
      String possibleCausesText,
      String nextChecksText,
      String proceedRecommendationText,
      boolean expertRequired,
      double confidenceScore) {}

  public record ImageResponse(String id, String fileName, String contentType, String storagePath) {}

  public record GuideLinkResponse(String title, String slug, String path) {}
}
