package com.selfinterior.api.integration;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationCallLogRepository
    extends JpaRepository<IntegrationCallLogEntity, UUID> {}
