package com.selfinterior.api.property;

import java.util.List;
import java.util.Map;

public record PropertyProviderResult(
    PropertyType propertyType,
    String apartmentName,
    Integer completionYear,
    Integer householdCount,
    List<Double> exclusiveAreaCandidates,
    List<Integer> roomCountCandidates,
    List<Integer> bathroomCountCandidates,
    String jibunAddress,
    List<ExternalRefPayload> externalRefs,
    Map<String, Object> rawPayload) {}
