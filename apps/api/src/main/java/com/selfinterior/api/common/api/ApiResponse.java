package com.selfinterior.api.common.api;

import java.util.Map;

public record ApiResponse<T>(boolean success, T data, Map<String, Object> meta, ApiError error) {
  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, data, Map.of(), null);
  }

  public static <T> ApiResponse<T> error(ApiError error) {
    return new ApiResponse<>(false, null, Map.of(), error);
  }
}
