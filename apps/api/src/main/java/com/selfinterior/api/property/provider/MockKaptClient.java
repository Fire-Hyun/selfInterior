package com.selfinterior.api.property.provider;

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
    return new PropertyProviderResult(
        PropertyType.APARTMENT,
        roadAddress.contains("헬리오") ? "헬리오시티" : "리센츠",
        roadAddress.contains("헬리오") ? 2018 : 2008,
        roadAddress.contains("헬리오") ? 9510 : 5563,
        List.of(59.97, 84.99),
        List.of(3),
        List.of(2),
        roadAddress.contains("헬리오") ? "서울특별시 송파구 가락동 913" : "서울특별시 송파구 잠실동 22",
        List.of(
            new ExternalRefPayload(
                "KAPT", "COMPLEX_CODE", roadAddress.contains("헬리오") ? "A10099999" : "A10027890")),
        Map.of("provider", "KAPT", "roadAddress", roadAddress));
  }
}
