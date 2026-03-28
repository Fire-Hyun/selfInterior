package com.selfinterior.api.address;

import com.selfinterior.api.address.AddressController.AddressSearchCandidateDto;
import com.selfinterior.api.address.AddressController.ComplexHintDto;

public final class AddressMapper {
  private AddressMapper() {}

  public static AddressSearchCandidateDto toDto(AddressCandidate candidate) {
    return new AddressSearchCandidateDto(
        candidate.displayName(),
        candidate.roadAddress(),
        candidate.jibunAddress(),
        candidate.propertyType().name(),
        candidate.lat(),
        candidate.lng(),
        candidate.dongCandidates(),
        candidate.hoCandidateRequired(),
        candidate.roadCode(),
        candidate.buildingMainNo(),
        candidate.buildingSubNo(),
        candidate.legalDongCode(),
        new ComplexHintDto(
            candidate.completionYear(), candidate.householdCount(), candidate.areaHints()));
  }
}
