package com.selfinterior.api.address;

import com.selfinterior.api.property.PropertyType;
import java.util.List;
import java.util.Map;

public record AddressCandidate(
    String displayName,
    String roadAddress,
    String jibunAddress,
    PropertyType propertyType,
    double lat,
    double lng,
    List<String> dongCandidates,
    boolean hoCandidateRequired,
    String roadCode,
    String buildingMainNo,
    String buildingSubNo,
    String legalDongCode,
    int completionYear,
    int householdCount,
    List<Double> areaHints,
    Map<String, Object> rawPayload) {}
