package com.selfinterior.api.address;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressResolutionLogRepository
    extends JpaRepository<AddressResolutionLogEntity, UUID> {}
