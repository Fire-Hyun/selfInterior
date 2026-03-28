package com.selfinterior.api.property.provider;

import com.selfinterior.api.property.PropertyProviderResult;

public interface BuildingHubClient {
  PropertyProviderResult resolve(String roadAddress);
}
