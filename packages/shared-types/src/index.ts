export type ProjectType = 'FULL' | 'PARTIAL' | 'ISSUE_CHECK';
export type LivingStatus = 'BEFORE_MOVE_IN' | 'OCCUPIED';
export type PropertyType = 'APARTMENT' | 'OFFICETEL' | 'VILLA' | 'HOUSE';
export type ConfidenceGrade = 'EXACT' | 'HIGH' | 'APPROX' | 'LOW';
export type FloorPlanSourceType =
  | 'OFFICIAL'
  | 'LICENSED'
  | 'USER_UPLOAD'
  | 'APPROX';
export type LicenseStatus =
  | 'APPROVED'
  | 'RESTRICTED'
  | 'USER_OWNED'
  | 'INTERNAL';
export type ProcessPlanStatus = 'IN_PROGRESS' | 'COMPLETED';
export type ProcessStepStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
export type ProcessTaskGroup = 'PREPARE' | 'DECISION' | 'NEXT';

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  meta: Record<string, unknown>;
  error: {
    code: string;
    message: string;
    requestId?: string;
  } | null;
}

export interface AddressSearchCandidate {
  displayName: string;
  roadAddress: string;
  jibunAddress: string;
  propertyType: PropertyType;
  lat: number;
  lng: number;
  dongCandidates: string[];
  hoCandidateRequired: boolean;
  roadCode: string;
  buildingMainNo: string;
  buildingSubNo: string;
  legalDongCode: string;
  complexHint: {
    completionYear: number;
    householdCount: number;
  };
}

export interface AddressSearchRequest {
  query: string;
}

export interface AddressSearchResponse {
  candidates: AddressSearchCandidate[];
}

export interface DetailOptionsRequest {
  roadCode: string;
  buildingMainNo: string;
  buildingSubNo: string;
  queryType: 'dong' | 'ho';
}

export interface DetailOptionsResponse {
  options: string[];
}

export interface PropertyExternalRef {
  provider: string;
  refType: string;
  externalKey: string;
}

export interface PropertySummary {
  propertyType: PropertyType;
  apartmentName: string;
  completionYear: number;
  householdCount: number;
  exclusiveAreaCandidates: number[];
  roomCountCandidates: number[];
  bathroomCountCandidates: number[];
  roadAddress: string;
  jibunAddress?: string;
  dongNo?: string;
  hoNo?: string;
}

export interface PropertyResolveRequest {
  roadAddress: string;
  dongNo?: string;
  hoNo?: string;
}

export interface PropertyResolveResponse {
  propertySummary: PropertySummary;
  externalRefs: PropertyExternalRef[];
}

export interface CreateProjectRequest {
  title: string;
  projectType: ProjectType;
  livingStatus: LivingStatus;
  budgetMin?: number;
  budgetMax?: number;
}

export interface ProjectSummary {
  id: string;
  title: string;
  projectType: ProjectType;
  livingStatus: LivingStatus;
  budgetMin?: number;
  budgetMax?: number;
  propertyAttached: boolean;
}

export interface CreateProjectResponse {
  project: ProjectSummary;
}

export interface ProjectPropertySnapshot {
  apartmentName: string;
  roadAddress: string;
  dongNo?: string;
  hoNo?: string;
  exclusiveAreaM2?: number;
  roomCount?: number;
  bathroomCount?: number;
}

export interface SelectedFloorPlanSnapshot {
  candidateId: string;
  layoutLabel: string;
  confidenceGrade: ConfidenceGrade;
  confidenceScore: number;
  sourceType: FloorPlanSourceType;
  licenseStatus: LicenseStatus;
  source: string;
}

export interface ProjectDetail extends ProjectSummary {
  property?: ProjectPropertySnapshot;
  selectedFloorPlan?: SelectedFloorPlanSnapshot;
  floorPlanCandidateCount: number;
}

export interface ProjectHomeSummary {
  id: string;
  title: string;
  projectType: ProjectType;
  livingStatus: LivingStatus;
  currentProcessStep?: string;
  onboardingCompleted: boolean;
}

export interface ProjectHomePropertyCard {
  apartmentName: string;
  roadAddress: string;
  dongNo?: string;
  hoNo?: string;
  completionYear?: number;
  householdCount?: number;
  exclusiveAreaM2?: number;
}

export interface ProjectHomeFloorPlanCard {
  candidateId: string;
  layoutLabel: string;
  confidenceGrade: ConfidenceGrade;
  confidenceScore: number;
  sourceType: FloorPlanSourceType;
  source: string;
  licenseStatus: LicenseStatus;
  candidateCount: number;
  structureSummary: string;
  manualCheckItems: string[];
}

export interface ProjectHomeAction {
  key: string;
  title: string;
  description: string;
  status: 'DONE' | 'READY' | 'BLOCKED' | 'UPCOMING';
  path: string;
}

export interface ProjectHomePlaceholderCard {
  title: string;
  status: string;
  description: string;
  primaryActionLabel: string;
  primaryActionPath: string;
}

export interface ProjectHomeResponse {
  project: ProjectHomeSummary;
  property?: ProjectHomePropertyCard;
  floorPlan?: ProjectHomeFloorPlanCard;
  nextActions: ProjectHomeAction[];
  recentQuestions: ProjectHomePlaceholderCard;
  recommendedExperts: ProjectHomePlaceholderCard;
}

export interface ProcessStepSummary {
  stepKey: string;
  title: string;
  status: ProcessStepStatus;
  sortOrder: number;
  durationDays: number;
  required: boolean;
  completedTaskCount: number;
  totalTaskCount: number;
}

export interface ProcessPlanResponse {
  planId: string;
  status: ProcessPlanStatus;
  currentStepKey?: string;
  progressPercent: number;
  generatedAt: string;
  steps: ProcessStepSummary[];
}

export interface ProcessTask {
  id: string;
  taskGroup: ProcessTaskGroup;
  itemOrder: number;
  title: string;
  description?: string;
  completed: boolean;
}

export interface ProcessPlanStepDetailResponse {
  planId: string;
  step: ProcessStepSummary;
  purposeText: string;
  startCheckIntro: string;
  decisionPoints: string[];
  selfWorkText: string;
  expertRequiredText: string;
  mistakesText: string;
  nextStepChecks: string[];
  tasks: ProcessTask[];
}

export interface ProcessTaskToggleRequest {
  completed: boolean;
}

export interface ProcessTaskToggleResponse {
  taskId: string;
  completed: boolean;
  stepStatus: ProcessStepStatus;
  currentStepKey?: string;
}

export interface AttachPropertyRequest {
  roadAddress: string;
  jibunAddress?: string;
  apartmentName: string;
  dongNo?: string;
  hoNo?: string;
  exclusiveAreaM2?: number;
  roomCount?: number;
  bathroomCount?: number;
  externalRefs: PropertyExternalRef[];
}

export interface FloorPlanResolveResponse {
  resolutionStatus: 'COMPLETED' | 'FAILED';
  candidateCount: number;
}

export interface FloorPlanCandidateSummary {
  id: string;
  sourceType: FloorPlanSourceType;
  matchType: string;
  confidenceScore: number;
  confidenceGrade: ConfidenceGrade;
  layoutLabel: string;
  exclusiveAreaM2?: number;
  licenseStatus: LicenseStatus;
  source: string;
  rawPayloadRef?: string;
  normalizedPlanRef?: string;
  manualCheckItems: string[];
}

export interface FloorPlanListResponse {
  selectedPlanId?: string;
  candidates: FloorPlanCandidateSummary[];
}

export interface FloorPlanSelectRequest {
  reason: string;
}

export interface FloorPlanSelectResponse {
  selectedPlanId: string;
}
