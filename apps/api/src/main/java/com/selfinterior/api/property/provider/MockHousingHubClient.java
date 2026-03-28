package com.selfinterior.api.property.provider;

import com.selfinterior.api.property.ExternalRefPayload;
import com.selfinterior.api.property.PropertyProviderResult;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockHousingHubClient implements HousingHubClient {
  @Override
  public PropertyProviderResult resolve(String roadAddress) {
    return new PropertyProviderResult(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        List.of(
            new ExternalRefPayload(
                "HOUSING_HUB", "PERMIT_PK", "HSG-" + Math.abs(roadAddress.hashCode()))),
        Map.of("provider", "HOUSING_HUB", "roadAddress", roadAddress));
  }
}
