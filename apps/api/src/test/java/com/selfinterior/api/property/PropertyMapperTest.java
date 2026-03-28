package com.selfinterior.api.property;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PropertyMapperTest {
  @Test
  void toResolveResponseBuildsAreaOptionsFromExclusiveAreas() {
    PropertyResolution resolution =
        new PropertyResolution(
            PropertyType.APARTMENT,
            "잠실 리센츠",
            2008,
            5563,
            List.of(59.97, 84.99),
            List.of(3),
            List.of(2),
            "서울특별시 송파구 올림픽로 135",
            "서울특별시 송파구 잠실동 22",
            null,
            null,
            List.of());

    var response = PropertyMapper.toResolveResponse(resolution);

    assertThat(response.propertySummary().areaOptions()).hasSize(2);
    assertThat(response.propertySummary().areaOptions().get(0).label()).isEqualTo("전용 59.97㎡");
    assertThat(response.propertySummary().areaOptions().get(1).exclusiveAreaM2()).isEqualTo(84.99);
    assertThat(response.propertySummary().areaOptions().get(1).roomCount()).isEqualTo(3);
    assertThat(response.propertySummary().areaOptions().get(1).bathroomCount()).isEqualTo(2);
  }
}
