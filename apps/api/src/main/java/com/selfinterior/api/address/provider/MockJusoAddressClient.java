package com.selfinterior.api.address.provider;

import com.selfinterior.api.address.AddressCandidate;
import com.selfinterior.api.property.PropertyType;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockJusoAddressClient implements JusoAddressClient {
  @Override
  public List<AddressCandidate> search(String query) {
    return List.of(
        new AddressCandidate(
            query.contains("헬리오") ? "헬리오시티" : "리센츠",
            query.contains("헬리오") ? "서울특별시 송파구 송파대로 345" : "서울특별시 송파구 올림픽로 135",
            query.contains("헬리오") ? "서울특별시 송파구 가락동 913" : "서울특별시 송파구 잠실동 22",
            PropertyType.APARTMENT,
            37.4975,
            127.1070,
            List.of("101", "102"),
            true,
            query.contains("헬리오") ? "117104321000" : "117103123456",
            query.contains("헬리오") ? "345" : "135",
            "0",
            "1171011100",
            query.contains("헬리오") ? 2018 : 2008,
            query.contains("헬리오") ? 9510 : 5563,
            Map.of("provider", "JUSO", "query", query)));
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
