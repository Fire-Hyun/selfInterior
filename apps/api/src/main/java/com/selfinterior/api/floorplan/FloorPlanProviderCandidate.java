package com.selfinterior.api.floorplan;

import java.util.List;
import java.util.Map;

public record FloorPlanProviderCandidate(
    String provider,
    LicenseStatus licenseStatus,
    AccessScope accessScope,
    String providerDocRef,
    String providerPlanKey,
    FloorPlanSourceType sourceType,
    String source,
    MatchType matchType,
    double confidenceScore,
    ConfidenceGrade confidenceGrade,
    Double exclusiveAreaM2,
    Double supplyAreaM2,
    Integer roomCount,
    Integer bathroomCount,
    String layoutLabel,
    Map<String, Object> rawPayload,
    List<String> manualCheckItems,
    Map<String, Object> normalizedPlan) {}
