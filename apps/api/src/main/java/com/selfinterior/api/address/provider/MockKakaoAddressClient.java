package com.selfinterior.api.address.provider;

import com.selfinterior.api.address.AddressCandidate;
import com.selfinterior.api.mock.MockApartmentCatalog;
import com.selfinterior.api.property.PropertyType;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockKakaoAddressClient implements KakaoAddressClient {
  @Override
  public List<AddressCandidate> search(String query) {
    return MockApartmentCatalog.search(query).stream()
        .limit(3)
        .map(
            complex ->
                new AddressCandidate(
                    complex.apartmentName(),
                    complex.roadAddress(),
                    complex.jibunAddress(),
                    PropertyType.APARTMENT,
                    complex.lat(),
                    complex.lng(),
                    complex.dongCandidates(),
                    false,
                    complex.roadCode(),
                    complex.buildingMainNo(),
                    complex.buildingSubNo(),
                    complex.legalDongCode(),
                    complex.completionYear(),
                    complex.householdCount(),
                    complex.areaHints(),
                    Map.of(
                        "provider", "KAKAO", "query", query, "complexCode", complex.complexCode())))
        .toList();
  }
}
