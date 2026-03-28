package com.selfinterior.api.property.provider;

import com.selfinterior.api.mock.MockApartmentCatalog;
import com.selfinterior.api.property.PropertyProviderResult;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockVworldClient implements VworldClient {
  @Override
  public PropertyProviderResult resolve(String roadAddress) {
    MockApartmentCatalog.MockApartmentComplex complex =
        MockApartmentCatalog.findByRoadAddress(roadAddress);

    return new PropertyProviderResult(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        complex.jibunAddress(),
        List.of(),
        Map.of("provider", "VWORLD", "roadAddress", roadAddress));
  }
}
