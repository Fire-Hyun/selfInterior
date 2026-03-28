package com.selfinterior.api.property.provider;

import com.selfinterior.api.property.PropertyProviderResult;

public interface KaptClient {
  PropertyProviderResult resolve(String roadAddress);
}
