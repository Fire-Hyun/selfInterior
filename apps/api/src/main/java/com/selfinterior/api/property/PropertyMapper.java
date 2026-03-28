package com.selfinterior.api.property;

import com.selfinterior.api.property.PropertyController.ExternalRefResponse;
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
