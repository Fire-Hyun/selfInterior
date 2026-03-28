package com.selfinterior.api.property;

import com.selfinterior.api.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping
@RequiredArgsConstructor
public class PropertyController {
  private final PropertyResolveService propertyResolveService;
  private final ProjectPropertyService projectPropertyService;

  @PostMapping("/api/v1/property/resolve")
  public ApiResponse<PropertyResolveResponse> resolve(
      @Valid @RequestBody PropertyResolveRequest request) {
    return ApiResponse.ok(propertyResolveService.resolve(request));
  }

  @PostMapping("/api/v1/projects/{projectId}/property")
  public ApiResponse<ProjectPropertyResponse> attachProperty(
      @PathVariable String projectId, @Valid @RequestBody AttachPropertyRequest request) {
    return ApiResponse.ok(projectPropertyService.attach(projectId, request));
  }

  public record PropertyResolveRequest(@NotBlank String roadAddress, String dongNo, String hoNo) {}

  public record PropertyResolveResponse(
      PropertySummaryResponse propertySummary, List<ExternalRefResponse> externalRefs) {}

  public record PropertySummaryResponse(
      String propertyType,
      String apartmentName,
      int completionYear,
      int householdCount,
      List<Double> exclusiveAreaCandidates,
      List<Integer> roomCountCandidates,
      List<Integer> bathroomCountCandidates,
      String roadAddress,
      String jibunAddress,
      String dongNo,
      String hoNo) {}

  public record ExternalRefResponse(String provider, String refType, String externalKey) {}

  public record AttachPropertyRequest(
      @NotBlank String roadAddress,
      String jibunAddress,
      @NotBlank String apartmentName,
      String dongNo,
      String hoNo,
      Double exclusiveAreaM2,
      Integer roomCount,
      Integer bathroomCount,
      List<ExternalRefResponse> externalRefs) {}

  public record ProjectPropertyResponse(String projectId, String propertyId) {}
}
