package com.selfinterior.api.property.provider;

import com.selfinterior.api.property.PropertyProviderResult;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockVworldClient implements VworldClient {
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
        roadAddress.contains("헬리오") ? "서울특별시 송파구 가락동 913" : "서울특별시 송파구 잠실동 22",
        List.of(),
        Map.of("provider", "VWORLD", "roadAddress", roadAddress));
  }
}
