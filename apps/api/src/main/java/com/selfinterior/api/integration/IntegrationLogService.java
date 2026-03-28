package com.selfinterior.api.integration;

import com.selfinterior.api.common.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntegrationLogService {
  private final IntegrationCallLogRepository integrationCallLogRepository;
  private final HttpServletRequest httpServletRequest;

  public UUID logSuccess(
      String provider,
      String operation,
      Map<String, Object> requestMeta,
      Map<String, Object> responseMeta,
      long latencyMs) {
    IntegrationCallLogEntity entity = new IntegrationCallLogEntity();
    entity.setProvider(provider);
    entity.setOperation(operation);
    entity.setRequestId((String) httpServletRequest.getAttribute(RequestIdFilter.REQUEST_ID_ATTR));
    entity.setStatusCode("OK");
    entity.setSuccess(true);
    entity.setLatencyMs((int) latencyMs);
    entity.setRequestMeta(requestMeta);
    entity.setResponseMeta(responseMeta);
    return integrationCallLogRepository.save(entity).getId();
  }

  public UUID logFailure(
      String provider,
      String operation,
      Map<String, Object> requestMeta,
      String errorMessage,
      long latencyMs) {
    IntegrationCallLogEntity entity = new IntegrationCallLogEntity();
    entity.setProvider(provider);
    entity.setOperation(operation);
    entity.setRequestId((String) httpServletRequest.getAttribute(RequestIdFilter.REQUEST_ID_ATTR));
    entity.setStatusCode("FAILED");
    entity.setSuccess(false);
    entity.setLatencyMs((int) latencyMs);
    entity.setErrorMessage(errorMessage);
    entity.setRequestMeta(requestMeta);
    entity.setResponseMeta(Map.of());
    return integrationCallLogRepository.save(entity).getId();
  }
}
