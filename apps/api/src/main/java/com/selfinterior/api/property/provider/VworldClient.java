package com.selfinterior.api.property.provider;

import com.selfinterior.api.property.PropertyProviderResult;

public interface VworldClient {
  PropertyProviderResult resolve(String roadAddress);
}
