package com.selfinterior.api.property.provider;

import com.selfinterior.api.mock.MockApartmentCatalog;
import com.selfinterior.api.property.ExternalRefPayload;
import com.selfinterior.api.property.PropertyProviderResult;
import com.selfinterior.api.property.PropertyType;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockKaptClient implements KaptClient {
  @Override
  public PropertyProviderResult resolve(String roadAddress) {
    MockApartmentCatalog.MockApartmentComplex complex =
        MockApartmentCatalog.findByRoadAddress(roadAddress);

    return new PropertyProviderResult(
        PropertyType.APARTMENT,
        complex.apartmentName(),
        complex.completionYear(),
        complex.householdCount(),
        complex.areaHints(),
        List.of(3),
        List.of(2),
        complex.jibunAddress(),
        List.of(new ExternalRefPayload("KAPT", "COMPLEX_CODE", complex.complexCode())),
        Map.of(
            "provider",
            "KAPT",
            "roadAddress",
            roadAddress,
            "complexCode",
            complex.complexCode(),
            "areaHints",
            complex.areaHints()));
  }
}
