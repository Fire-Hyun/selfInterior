package com.selfinterior.api.property;

import com.selfinterior.api.address.AddressResolutionLogEntity;
import com.selfinterior.api.address.AddressResolutionLogRepository;
import com.selfinterior.api.integration.IntegrationLogService;
import com.selfinterior.api.property.PropertyController.PropertyResolveRequest;
import com.selfinterior.api.property.PropertyController.PropertyResolveResponse;
import com.selfinterior.api.property.provider.BuildingHubClient;
import com.selfinterior.api.property.provider.HousingHubClient;
import com.selfinterior.api.property.provider.KaptClient;
import com.selfinterior.api.property.provider.VworldClient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropertyResolveService {
  private final KaptClient kaptClient;
  private final BuildingHubClient buildingHubClient;
  private final HousingHubClient housingHubClient;
  private final VworldClient vworldClient;
  private final IntegrationLogService integrationLogService;
  private final AddressResolutionLogRepository addressResolutionLogRepository;

  public PropertyResolveResponse resolve(PropertyResolveRequest request) {
    List<PropertyProviderResult> fragments = new ArrayList<>();
    fragments.add(
        callProvider(
            "KAPT",
            "property-resolve",
            request.roadAddress(),
            () -> kaptClient.resolve(request.roadAddress())));
    fragments.add(
        callProvider(
            "BUILDING_HUB",
            "property-resolve",
            request.roadAddress(),
            () -> buildingHubClient.resolve(request.roadAddress())));
    fragments.add(
        callProvider(
            "HOUSING_HUB",
            "property-resolve",
            request.roadAddress(),
            () -> housingHubClient.resolve(request.roadAddress())));
    fragments.add(
        callProvider(
            "VWORLD",
            "property-resolve",
            request.roadAddress(),
            () -> vworldClient.resolve(request.roadAddress())));

    PropertyProviderResult primary =
        fragments.stream()
            .filter(fragment -> fragment != null && fragment.apartmentName() != null)
            .findFirst()
            .orElse(null);

    String apartmentName = primary != null ? primary.apartmentName() : "미확인 공동주택";
    PropertyType propertyType = primary != null ? primary.propertyType() : PropertyType.APARTMENT;
    int completionYear =
        fragments.stream()
            .filter(fragment -> fragment != null && fragment.completionYear() != null)
            .map(PropertyProviderResult::completionYear)
            .findFirst()
            .orElse(0);
    int householdCount =
        fragments.stream()
            .filter(fragment -> fragment != null && fragment.householdCount() != null)
            .map(PropertyProviderResult::householdCount)
            .findFirst()
            .orElse(0);
    List<Double> exclusiveAreaCandidates =
        fragments.stream()
            .filter(fragment -> fragment != null && fragment.exclusiveAreaCandidates() != null)
            .findFirst()
            .map(PropertyProviderResult::exclusiveAreaCandidates)
            .orElse(List.of(59.97, 84.99));
    List<Integer> roomCountCandidates =
        fragments.stream()
            .filter(fragment -> fragment != null && fragment.roomCountCandidates() != null)
            .findFirst()
            .map(PropertyProviderResult::roomCountCandidates)
            .orElse(List.of(3));
    List<Integer> bathroomCountCandidates =
        fragments.stream()
            .filter(fragment -> fragment != null && fragment.bathroomCountCandidates() != null)
            .findFirst()
            .map(PropertyProviderResult::bathroomCountCandidates)
            .orElse(List.of(2));
    String jibunAddress =
        fragments.stream()
            .filter(fragment -> fragment != null && fragment.jibunAddress() != null)
            .findFirst()
            .map(PropertyProviderResult::jibunAddress)
            .orElse(request.roadAddress());

    List<ExternalRefPayload> refs =
        fragments.stream()
            .filter(fragment -> fragment != null && fragment.externalRefs() != null)
            .flatMap(fragment -> fragment.externalRefs().stream())
            .toList();

    Map<String, Object> rawPayload = new LinkedHashMap<>();
    int index = 0;
    for (PropertyProviderResult fragment : fragments) {
      rawPayload.put("provider_" + index++, fragment == null ? Map.of() : fragment.rawPayload());
    }

    AddressResolutionLogEntity logEntity = new AddressResolutionLogEntity();
    logEntity.setInputQuery(request.roadAddress());
    logEntity.setNormalizedRoadAddress(request.roadAddress());
    logEntity.setNormalizedJibunAddress(jibunAddress);
    logEntity.setRoadCode("117103123456");
    logEntity.setBuildingMainNo("135");
    logEntity.setBuildingSubNo("0");
    logEntity.setLegalDongCode("1171010100");
    logEntity.setSource("PROPERTY_RESOLVE");
    logEntity.setConfidenceScore(BigDecimal.valueOf(88.0));
    logEntity.setRawPayload(rawPayload);
    addressResolutionLogRepository.save(logEntity);

    return PropertyMapper.toResolveResponse(
        new PropertyResolution(
            propertyType,
            apartmentName,
            completionYear,
            householdCount,
            exclusiveAreaCandidates,
            roomCountCandidates,
            bathroomCountCandidates,
            request.roadAddress(),
            jibunAddress,
            request.dongNo(),
            request.hoNo(),
            refs));
  }

  private PropertyProviderResult callProvider(
      String provider, String operation, String roadAddress, ProviderCall providerCall) {
    long start = System.currentTimeMillis();
    try {
      PropertyProviderResult result = providerCall.call();
      integrationLogService.logSuccess(
          provider,
          operation,
          Map.of("roadAddress", roadAddress),
          Map.of("apartmentName", result.apartmentName()),
          System.currentTimeMillis() - start);
      return result;
    } catch (RuntimeException exception) {
      integrationLogService.logFailure(
          provider,
          operation,
          Map.of("roadAddress", roadAddress),
          exception.getMessage(),
          System.currentTimeMillis() - start);
      return null;
    }
  }

  @FunctionalInterface
  interface ProviderCall {
    PropertyProviderResult call();
  }
}
