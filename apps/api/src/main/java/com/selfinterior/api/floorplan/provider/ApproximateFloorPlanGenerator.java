package com.selfinterior.api.floorplan.provider;

import com.selfinterior.api.floorplan.FloorPlanProviderCandidate;
import com.selfinterior.api.project.ProjectEntity;
import com.selfinterior.api.property.PropertyEntity;

public interface ApproximateFloorPlanGenerator {
  FloorPlanProviderCandidate generate(ProjectEntity project, PropertyEntity property);
}
