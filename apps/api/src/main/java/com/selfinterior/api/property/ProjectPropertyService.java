package com.selfinterior.api.property;

import com.selfinterior.api.common.api.ApiException;
import com.selfinterior.api.common.api.ErrorCode;
import com.selfinterior.api.project.ProjectRepository;
import com.selfinterior.api.property.PropertyController.AttachPropertyRequest;
import com.selfinterior.api.property.PropertyController.ProjectPropertyResponse;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectPropertyService {
  private final ProjectRepository projectRepository;
  private final PropertyRepository propertyRepository;
  private final ExternalPropertyRefRepository externalPropertyRefRepository;

  @Transactional
  public ProjectPropertyResponse attach(String projectId, AttachPropertyRequest request) {
    UUID parsedProjectId = UUID.fromString(projectId);
    projectRepository
        .findById(parsedProjectId)
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

    PropertyEntity entity =
        propertyRepository.findByProjectId(parsedProjectId).orElseGet(PropertyEntity::new);
    entity.setProjectId(parsedProjectId);
    entity.setPropertyType(PropertyType.APARTMENT);
    entity.setCountryCode("KR");
    entity.setRoadAddress(request.roadAddress());
    entity.setJibunAddress(request.jibunAddress());
    entity.setApartmentName(request.apartmentName());
    entity.setDongNo(request.dongNo());
    entity.setHoNo(request.hoNo());
    entity.setExclusiveAreaM2(
        request.exclusiveAreaM2() == null ? null : BigDecimal.valueOf(request.exclusiveAreaM2()));
    entity.setRoomCount(request.roomCount());
    entity.setBathroomCount(request.bathroomCount());
    entity.setRawSummary(
        Map.of("roadAddress", request.roadAddress(), "apartmentName", request.apartmentName()));
    PropertyEntity saved = propertyRepository.save(entity);

    externalPropertyRefRepository.deleteByPropertyId(saved.getId());
    externalPropertyRefRepository.saveAll(
        PropertyMapper.toEntities(request.externalRefs(), saved.getId()));

    return new ProjectPropertyResponse(parsedProjectId.toString(), saved.getId().toString());
  }
}
