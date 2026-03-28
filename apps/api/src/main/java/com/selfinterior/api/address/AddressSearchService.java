package com.selfinterior.api.address;

import com.selfinterior.api.address.AddressController.AddressSearchRequest;
import com.selfinterior.api.address.AddressController.AddressSearchResponse;
import com.selfinterior.api.address.AddressController.DetailOptionsRequest;
import com.selfinterior.api.address.AddressController.DetailOptionsResponse;
import com.selfinterior.api.address.provider.JusoAddressClient;
import com.selfinterior.api.address.provider.KakaoAddressClient;
import com.selfinterior.api.integration.IntegrationLogService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressSearchService {
  private final KakaoAddressClient kakaoAddressClient;
  private final JusoAddressClient jusoAddressClient;
  private final IntegrationLogService integrationLogService;
  private final AddressResolutionLogRepository addressResolutionLogRepository;

  public AddressSearchResponse search(AddressSearchRequest request) {
    List<AddressCandidate> candidates = callPrimary(request.query());
    if (candidates.isEmpty()) {
      candidates = callFallback(request.query());
    }

    if (!candidates.isEmpty()) {
      AddressCandidate first = candidates.get(0);
      AddressResolutionLogEntity logEntity = new AddressResolutionLogEntity();
      logEntity.setInputQuery(request.query());
      logEntity.setNormalizedRoadAddress(first.roadAddress());
      logEntity.setNormalizedJibunAddress(first.jibunAddress());
      logEntity.setRoadCode(first.roadCode());
      logEntity.setBuildingMainNo(first.buildingMainNo());
      logEntity.setBuildingSubNo(first.buildingSubNo());
      logEntity.setLegalDongCode(first.legalDongCode());
      logEntity.setSource("ADDRESS_SEARCH");
      logEntity.setConfidenceScore(BigDecimal.valueOf(82.0));
      logEntity.setRawPayload(first.rawPayload());
      addressResolutionLogRepository.save(logEntity);
    }

    return new AddressSearchResponse(candidates.stream().map(AddressMapper::toDto).toList());
  }

  public DetailOptionsResponse detailOptions(DetailOptionsRequest request) {
    long start = System.currentTimeMillis();
    List<String> options =
        jusoAddressClient.detailOptions(
            request.roadCode(),
            request.buildingMainNo(),
            request.buildingSubNo(),
            request.queryType());
    integrationLogService.logSuccess(
        "JUSO",
        "detail-options",
        Map.of(
            "roadCode", request.roadCode(),
            "buildingMainNo", request.buildingMainNo(),
            "buildingSubNo", request.buildingSubNo(),
            "queryType", request.queryType()),
        Map.of("options", options),
        System.currentTimeMillis() - start);
    return new DetailOptionsResponse(options);
  }

  private List<AddressCandidate> callPrimary(String query) {
    long start = System.currentTimeMillis();
    try {
      List<AddressCandidate> result = kakaoAddressClient.search(query);
      integrationLogService.logSuccess(
          "KAKAO",
          "address-search",
          Map.of("query", query),
          Map.of("count", result.size()),
          System.currentTimeMillis() - start);
      return result;
    } catch (RuntimeException exception) {
      integrationLogService.logFailure(
          "KAKAO",
          "address-search",
          Map.of("query", query),
          exception.getMessage(),
          System.currentTimeMillis() - start);
      return List.of();
    }
  }

  private List<AddressCandidate> callFallback(String query) {
    long start = System.currentTimeMillis();
    try {
      List<AddressCandidate> result = jusoAddressClient.search(query);
      integrationLogService.logSuccess(
          "JUSO",
          "address-search",
          Map.of("query", query),
          Map.of("count", result.size()),
          System.currentTimeMillis() - start);
      return result;
    } catch (RuntimeException exception) {
      integrationLogService.logFailure(
          "JUSO",
          "address-search",
          Map.of("query", query),
          exception.getMessage(),
          System.currentTimeMillis() - start);
      return List.of();
    }
  }
}
