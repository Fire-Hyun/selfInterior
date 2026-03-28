package com.selfinterior.api.address;

import com.selfinterior.api.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/address")
@RequiredArgsConstructor
public class AddressController {
  private final AddressSearchService addressSearchService;

  @PostMapping("/search")
  public ApiResponse<AddressSearchResponse> search(
      @Valid @RequestBody AddressSearchRequest request) {
    return ApiResponse.ok(addressSearchService.search(request));
  }

  @PostMapping("/detail-options")
  public ApiResponse<DetailOptionsResponse> detailOptions(
      @Valid @RequestBody DetailOptionsRequest request) {
    return ApiResponse.ok(addressSearchService.detailOptions(request));
  }

  public record AddressSearchRequest(@NotBlank String query) {}

  public record AddressSearchResponse(List<AddressSearchCandidateDto> candidates) {}

  public record AddressSearchCandidateDto(
      String displayName,
      String roadAddress,
      String jibunAddress,
      String propertyType,
      double lat,
      double lng,
      List<String> dongCandidates,
      boolean hoCandidateRequired,
      String roadCode,
      String buildingMainNo,
      String buildingSubNo,
      String legalDongCode,
      ComplexHintDto complexHint) {}

  public record ComplexHintDto(int completionYear, int householdCount, List<Double> areaHints) {}

  public record DetailOptionsRequest(
      @NotBlank String roadCode,
      @NotBlank String buildingMainNo,
      @NotBlank String buildingSubNo,
      @NotBlank String queryType) {}

  public record DetailOptionsResponse(List<String> options) {}
}
