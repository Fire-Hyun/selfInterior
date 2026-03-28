package com.selfinterior.api.property.provider;

import com.selfinterior.api.property.PropertyProviderResult;

public interface HousingHubClient {
  PropertyProviderResult resolve(String roadAddress);
}
