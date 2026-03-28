package com.selfinterior.api.property;

import java.util.List;

public record PropertyResolution(
    PropertyType propertyType,
    String apartmentName,
    int completionYear,
    int householdCount,
    List<Double> exclusiveAreaCandidates,
    List<Integer> roomCountCandidates,
    List<Integer> bathroomCountCandidates,
    String roadAddress,
    String jibunAddress,
    String dongNo,
    String hoNo,
    List<ExternalRefPayload> externalRefs) {}
