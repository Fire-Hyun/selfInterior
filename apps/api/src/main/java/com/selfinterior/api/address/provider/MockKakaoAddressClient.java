package com.selfinterior.api.address.provider;

import com.selfinterior.api.address.AddressCandidate;
import com.selfinterior.api.property.PropertyType;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockKakaoAddressClient implements KakaoAddressClient {
  @Override
  public List<AddressCandidate> search(String query) {
    if (!(query.contains("리센츠") || query.contains("잠실"))) {
      return List.of();
    }
    return List.of(
        new AddressCandidate(
            "리센츠",
            "서울특별시 송파구 올림픽로 135",
            "서울특별시 송파구 잠실동 22",
            PropertyType.APARTMENT,
            37.5163,
            127.1012,
            List.of("201", "202"),
            true,
            "117103123456",
            "135",
            "0",
            "1171010100",
            2008,
            5563,
            Map.of("provider", "KAKAO", "query", query)));
  }
}
