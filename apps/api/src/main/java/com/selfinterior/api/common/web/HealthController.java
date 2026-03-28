package com.selfinterior.api.common.web;

import com.selfinterior.api.common.api.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
  @GetMapping
  public ApiResponse<Map<String, String>> health() {
    return ApiResponse.ok(Map.of("status", "UP"));
  }
}
