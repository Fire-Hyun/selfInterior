package com.selfinterior.api.address.provider;

import com.selfinterior.api.address.AddressCandidate;
import com.selfinterior.api.mock.MockApartmentCatalog;
import com.selfinterior.api.property.PropertyType;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockJusoAddressClient implements JusoAddressClient {
  @Override
  public List<AddressCandidate> search(String query) {
    List<MockApartmentCatalog.MockApartmentComplex> complexes = MockApartmentCatalog.search(query);
    if (complexes.isEmpty()) {
      complexes = MockApartmentCatalog.search("");
    }

    return complexes.stream()
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
                        "provider", "JUSO", "query", query, "complexCode", complex.complexCode())))
        .toList();
  }

  @Override
  public List<String> detailOptions(
      String roadCode, String buildingMainNo, String buildingSubNo, String queryType) {
    if ("dong".equalsIgnoreCase(queryType)) {
      return List.of("101", "102", "201", "202");
    }
    return List.of("701", "902", "1203", "1501");
  }
}
