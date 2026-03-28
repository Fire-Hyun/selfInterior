package com.selfinterior.api.property;

import com.selfinterior.api.property.PropertyController.ExternalRefResponse;
import com.selfinterior.api.property.PropertyController.PropertyAreaOptionResponse;
import com.selfinterior.api.property.PropertyController.PropertyResolveResponse;
import com.selfinterior.api.property.PropertyController.PropertySummaryResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PropertyMapper {
  private PropertyMapper() {}

  public static PropertyResolveResponse toResolveResponse(PropertyResolution resolution) {
    return new PropertyResolveResponse(
        new PropertySummaryResponse(
            resolution.propertyType().name(),
            resolution.apartmentName(),
            resolution.completionYear(),
            resolution.householdCount(),
            toAreaOptions(resolution),
            resolution.exclusiveAreaCandidates(),
            resolution.roomCountCandidates(),
            resolution.bathroomCountCandidates(),
            resolution.roadAddress(),
            resolution.jibunAddress(),
            resolution.dongNo(),
            resolution.hoNo()),
        resolution.externalRefs().stream()
            .map(ref -> new ExternalRefResponse(ref.provider(), ref.refType(), ref.externalKey()))
            .toList());
  }

  private static List<PropertyAreaOptionResponse> toAreaOptions(PropertyResolution resolution) {
    int roomCount =
        resolution.roomCountCandidates().isEmpty() ? 0 : resolution.roomCountCandidates().get(0);
    int bathroomCount =
        resolution.bathroomCountCandidates().isEmpty()
            ? 0
            : resolution.bathroomCountCandidates().get(0);

    return resolution.exclusiveAreaCandidates().stream()
        .map(
            area ->
                new PropertyAreaOptionResponse(
                    formatAreaLabel(area), area, null, roomCount, bathroomCount))
        .toList();
  }

  private static String formatAreaLabel(Double area) {
    if (area == null) {
      return "Area check required";
    }

    if (Math.floor(area) == area) {
      return "Area " + area.intValue() + "m2";
    }

    return "Area " + area + "m2";
  }

  public static List<ExternalPropertyRefEntity> toEntities(
      List<PropertyController.ExternalRefResponse> refs, UUID propertyId) {
    if (refs == null) {
      return List.of();
    }
    return refs.stream()
        .map(
            ref -> {
              ExternalPropertyRefEntity entity = new ExternalPropertyRefEntity();
              entity.setPropertyId(propertyId);
              entity.setProvider(ref.provider());
              entity.setRefType(ref.refType());
              entity.setExternalKey(ref.externalKey());
              entity.setMetadata(Map.of());
              return entity;
            })
        .toList();
  }
}
