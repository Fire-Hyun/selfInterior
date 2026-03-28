package com.selfinterior.api.floorplan.provider;

import com.selfinterior.api.floorplan.FloorPlanProviderCandidate;
import com.selfinterior.api.project.ProjectEntity;
import com.selfinterior.api.property.PropertyEntity;
import java.util.List;

public interface OfficialFloorPlanClient {
  List<FloorPlanProviderCandidate> fetch(ProjectEntity project, PropertyEntity property);
}
