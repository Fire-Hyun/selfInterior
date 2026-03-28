package com.selfinterior.api.mock;

import java.util.List;
import java.util.Locale;

public final class MockApartmentCatalog {
  private static final String JAMSIL_RECENTS = "\uC7A0\uC2E4 \uB9AC\uC13C\uCE20";
  private static final String SONGPA_HELIO_CITY = "\uC1A1\uD30C \uD5EC\uB9AC\uC624\uC2DC\uD2F0";
  private static final String MAPO_RAEMIAN_PRUGIO =
      "\uB9C8\uD3EC \uB798\uBBF8\uC548 \uD478\uB974\uC9C0\uC624";

  private static final List<MockApartmentComplex> COMPLEXES =
      List.of(
          new MockApartmentComplex(
              JAMSIL_RECENTS,
              "\uC11C\uC6B8\uD2B9\uBCC4\uC2DC \uC1A1\uD30C\uAD6C \uC62C\uB9BC\uD53D\uB85C 135",
              "\uC11C\uC6B8\uD2B9\uBCC4\uC2DC \uC1A1\uD30C\uAD6C \uC7A0\uC2E4\uB3D9 22",
              37.5163,
              127.1012,
              "117103123456",
              "135",
              "0",
              "1171010100",
              2008,
              5563,
              List.of("201", "202"),
              List.of(59.97, 84.99, 109.92),
              "A10027890",
              List.of("recents", "jamsil recents")),
          new MockApartmentComplex(
              SONGPA_HELIO_CITY,
              "\uC11C\uC6B8\uD2B9\uBCC4\uC2DC \uC1A1\uD30C\uAD6C \uC1A1\uD30C\uB300\uB85C 345",
              "\uC11C\uC6B8\uD2B9\uBCC4\uC2DC \uC1A1\uD30C\uAD6C \uAC00\uB77D\uB3D9 913",
              37.4975,
              127.1070,
              "117104321000",
              "345",
              "0",
              "1171011100",
              2018,
              9510,
              List.of("101", "102"),
              List.of(39.68, 59.84, 84.81),
              "A10099999",
              List.of("helio", "helio city")),
          new MockApartmentComplex(
              MAPO_RAEMIAN_PRUGIO,
              "\uC11C\uC6B8\uD2B9\uBCC4\uC2DC \uB9C8\uD3EC\uAD6C \uB9C8\uD3EC\uB300\uB85C 195",
              "\uC11C\uC6B8\uD2B9\uBCC4\uC2DC \uB9C8\uD3EC\uAD6C \uC544\uD604\uB3D9 777",
              37.5536,
              126.9568,
              "114401020000",
              "195",
              "0",
              "1144012300",
              2014,
              3885,
              List.of("101", "102"),
              List.of(59.94, 84.96, 114.98),
              "A10056000",
              List.of("mapo raemian", "raemian prugio")));

  private MockApartmentCatalog() {}

  public static List<MockApartmentComplex> search(String query) {
    String normalizedQuery = normalize(query);
    if (normalizedQuery.isBlank()) {
      return COMPLEXES.subList(0, Math.min(3, COMPLEXES.size()));
    }

    List<MockApartmentComplex> matched =
        COMPLEXES.stream()
            .filter(
                complex ->
                    normalize(complex.apartmentName()).contains(normalizedQuery)
                        || normalize(complex.roadAddress()).contains(normalizedQuery)
                        || normalize(complex.jibunAddress()).contains(normalizedQuery)
                        || complex.aliases().stream()
                            .map(MockApartmentCatalog::normalize)
                            .anyMatch(alias -> alias.contains(normalizedQuery)))
            .toList();

    if (!matched.isEmpty()) {
      return matched;
    }

    return COMPLEXES.stream()
        .filter(
            complex ->
                normalizedQuery.contains(shortToken(complex.apartmentName()))
                    || shortToken(complex.apartmentName()).contains(normalizedQuery))
        .toList();
  }

  public static MockApartmentComplex findByRoadAddress(String roadAddress) {
    String normalizedRoadAddress = normalize(roadAddress);
    return COMPLEXES.stream()
        .filter(complex -> normalize(complex.roadAddress()).equals(normalizedRoadAddress))
        .findFirst()
        .orElse(COMPLEXES.get(0));
  }

  private static String normalize(String value) {
    return value == null
        ? ""
        : value.replace(" ", "").replace("-", "").toLowerCase(Locale.ROOT).trim();
  }

  private static String shortToken(String apartmentName) {
    String normalized = normalize(apartmentName);
    return normalized
        .replace(normalize("\uC7A0\uC2E4"), "")
        .replace(normalize("\uC1A1\uD30C"), "")
        .replace(normalize("\uB9C8\uD3EC"), "");
  }

  public record MockApartmentComplex(
      String apartmentName,
      String roadAddress,
      String jibunAddress,
      double lat,
      double lng,
      String roadCode,
      String buildingMainNo,
      String buildingSubNo,
      String legalDongCode,
      int completionYear,
      int householdCount,
      List<String> dongCandidates,
      List<Double> areaHints,
      String complexCode,
      List<String> aliases) {}
}
