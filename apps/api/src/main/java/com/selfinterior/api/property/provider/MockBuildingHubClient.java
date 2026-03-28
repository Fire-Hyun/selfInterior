package com.selfinterior.api.property.provider;

import com.selfinterior.api.property.ExternalRefPayload;
import com.selfinterior.api.property.PropertyProviderResult;
import com.selfinterior.api.property.PropertyType;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockBuildingHubClient implements BuildingHubClient {
  @Override
  public PropertyProviderResult resolve(String roadAddress) {
    return new PropertyProviderResult(
        PropertyType.APARTMENT,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        List.of(
            new ExternalRefPayload(
                "BUILDING_HUB", "BLD_KEY", "BLD-" + Math.abs(roadAddress.hashCode()))),
        Map.of("provider", "BUILDING_HUB", "roadAddress", roadAddress));
  }
}
