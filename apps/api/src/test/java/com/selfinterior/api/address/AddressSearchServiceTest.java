package com.selfinterior.api.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.selfinterior.api.address.AddressController.AddressSearchRequest;
import com.selfinterior.api.address.provider.JusoAddressClient;
import com.selfinterior.api.address.provider.KakaoAddressClient;
import com.selfinterior.api.integration.IntegrationLogService;
import com.selfinterior.api.property.PropertyType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddressSearchServiceTest {
  @Mock private KakaoAddressClient kakaoAddressClient;
  @Mock private JusoAddressClient jusoAddressClient;
  @Mock private IntegrationLogService integrationLogService;
  @Mock private AddressResolutionLogRepository addressResolutionLogRepository;

  @InjectMocks private AddressSearchService addressSearchService;

  @Test
  void fallsBackToJusoWhenKakaoReturnsEmpty() {
    when(kakaoAddressClient.search("heliocity")).thenReturn(List.of());
    when(jusoAddressClient.search("heliocity"))
        .thenReturn(
            List.of(
                new AddressCandidate(
                    "Helio City",
                    "345 Songpa-daero",
                    "913 Garak-dong",
                    PropertyType.APARTMENT,
                    37.49,
                    127.10,
                    List.of("101"),
                    true,
                    "117104321000",
                    "345",
                    "0",
                    "1171011100",
                    2018,
                    9510,
                    List.of(59.84, 84.81),
                    Map.of("provider", "JUSO"))));
    when(integrationLogService.logSuccess(any(), any(), any(), any(), any(Long.class)))
        .thenReturn(UUID.randomUUID());
    when(addressResolutionLogRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = addressSearchService.search(new AddressSearchRequest("heliocity"));

    assertThat(response.candidates()).hasSize(1);
    assertThat(response.candidates().get(0).displayName()).isEqualTo("Helio City");
    assertThat(response.candidates().get(0).complexHint().areaHints())
        .containsExactly(59.84, 84.81);
  }
}
