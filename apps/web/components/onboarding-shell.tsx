'use client';

import type {
  AddressSearchCandidate,
  AddressSearchRequest,
  AddressSearchResponse,
  AttachPropertyRequest,
  CreateProjectRequest,
  CreateProjectResponse,
  FloorPlanListResponse,
  FloorPlanResolveResponse,
  FloorPlanSelectRequest,
  ProjectDetail,
  ProjectSummary,
  PropertyAreaOption,
  PropertyResolveRequest,
  PropertyResolveResponse,
} from '@selfinterior/shared-types';
import Link from 'next/link';
import { startTransition, useEffect, useMemo, useState } from 'react';
import { apiRequest } from '@/lib/api';

export function OnboardingShell() {
  const [query, setQuery] = useState('리센츠');
  const [searchResults, setSearchResults] = useState<AddressSearchCandidate[]>(
    [],
  );
  const [selectedCandidate, setSelectedCandidate] =
    useState<AddressSearchCandidate | null>(null);
  const [propertySummary, setPropertySummary] = useState<
    PropertyResolveResponse['propertySummary'] | null
  >(null);
  const [selectedAreaLabel, setSelectedAreaLabel] = useState('');
  const [externalRefs, setExternalRefs] = useState<
    PropertyResolveResponse['externalRefs']
  >([]);
  const [projectTitle, setProjectTitle] = useState('우리 집 인테리어');
  const [projects, setProjects] = useState<ProjectSummary[]>([]);
  const [createdProject, setCreatedProject] = useState<ProjectSummary | null>(
    null,
  );
  const [projectDetail, setProjectDetail] = useState<ProjectDetail | null>(
    null,
  );
  const [floorPlans, setFloorPlans] = useState<FloorPlanListResponse | null>(
    null,
  );
  const [busyStage, setBusyStage] = useState('');
  const [error, setError] = useState('');

  const selectedArea = useMemo<PropertyAreaOption | null>(
    () =>
      propertySummary?.areaOptions.find(
        (option) => option.label === selectedAreaLabel,
      ) ?? null,
    [propertySummary, selectedAreaLabel],
  );

  useEffect(() => {
    startTransition(() => {
      void loadProjects();
    });
  }, []);

  const canCreateProject = Boolean(
    selectedCandidate && propertySummary && selectedArea,
  );

  async function loadProjects() {
    try {
      const response = await apiRequest<{ projects: ProjectSummary[] }>(
        '/api/v1/projects',
      );
      setProjects(response.data?.projects ?? []);
    } catch {
      setProjects([]);
    }
  }

  async function loadProjectDetail(projectId: string) {
    const response = await apiRequest<ProjectDetail>(
      `/api/v1/projects/${projectId}`,
    );
    setProjectDetail(response.data ?? null);
  }

  async function searchAddress() {
    setBusyStage('아파트 단지를 검색하고 있습니다...');
    setError('');
    setSearchResults([]);
    setSelectedCandidate(null);
    setPropertySummary(null);
    setSelectedAreaLabel('');
    setExternalRefs([]);
    setFloorPlans(null);
    setProjectDetail(null);

    try {
      const payload: AddressSearchRequest = { query };
      const response = await apiRequest<AddressSearchResponse>(
        '/api/v1/address/search',
        {
          method: 'POST',
          body: JSON.stringify(payload),
        },
      );
      setSearchResults(response.data?.candidates ?? []);
      setCreatedProject(null);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '단지 검색에 실패했습니다.',
      );
    } finally {
      setBusyStage('');
    }
  }

  async function chooseCandidate(candidate: AddressSearchCandidate) {
    setBusyStage('선택한 단지의 평형 정보를 불러오고 있습니다...');
    setError('');
    setSelectedCandidate(candidate);
    setPropertySummary(null);
    setSelectedAreaLabel('');
    setExternalRefs([]);
    setFloorPlans(null);
    setProjectDetail(null);

    try {
      const payload: PropertyResolveRequest = {
        roadAddress: candidate.roadAddress,
      };
      const response = await apiRequest<PropertyResolveResponse>(
        '/api/v1/property/resolve',
        {
          method: 'POST',
          body: JSON.stringify(payload),
        },
      );
      const summary = response.data?.propertySummary ?? null;
      setPropertySummary(summary);
      setExternalRefs(response.data?.externalRefs ?? []);
      setSelectedAreaLabel(summary?.areaOptions[0]?.label ?? '');
      if (summary?.apartmentName) {
        setProjectTitle(`${summary.apartmentName} 인테리어`);
      }
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '평형 정보를 불러오지 못했습니다.',
      );
    } finally {
      setBusyStage('');
    }
  }

  async function createProjectFlow() {
    if (!selectedCandidate || !propertySummary || !selectedArea) {
      return;
    }

    setBusyStage('프로젝트와 도면 후보를 준비하고 있습니다...');
    setError('');

    try {
      const createProjectPayload: CreateProjectRequest = {
        title: projectTitle,
        projectType: 'FULL',
        livingStatus: 'BEFORE_MOVE_IN',
        budgetMin: 30000000,
        budgetMax: 60000000,
      };
      const projectResponse = await apiRequest<CreateProjectResponse>(
        '/api/v1/projects',
        {
          method: 'POST',
          body: JSON.stringify(createProjectPayload),
        },
      );

      const project = projectResponse.data?.project;
      if (!project) {
        throw new Error('프로젝트 생성 응답이 비어 있습니다.');
      }

      setCreatedProject(project);

      const attachPropertyPayload: AttachPropertyRequest = {
        roadAddress: propertySummary.roadAddress,
        jibunAddress: propertySummary.jibunAddress,
        apartmentName: propertySummary.apartmentName,
        exclusiveAreaM2: selectedArea.exclusiveAreaM2,
        roomCount: selectedArea.roomCount,
        bathroomCount: selectedArea.bathroomCount,
        externalRefs,
      };

      await apiRequest(`/api/v1/projects/${project.id}/property`, {
        method: 'POST',
        body: JSON.stringify(attachPropertyPayload),
      });

      await apiRequest<FloorPlanResolveResponse>(
        `/api/v1/projects/${project.id}/floor-plans/resolve`,
        {
          method: 'POST',
          body: JSON.stringify({}),
        },
      );

      const floorPlanResponse = await apiRequest<FloorPlanListResponse>(
        `/api/v1/projects/${project.id}/floor-plans`,
      );
      setFloorPlans(floorPlanResponse.data ?? null);
      await loadProjectDetail(project.id);
      await loadProjects();
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '프로젝트 생성 흐름에 실패했습니다.',
      );
    } finally {
      setBusyStage('');
    }
  }

  async function selectFloorPlan(candidateId: string) {
    if (!createdProject) {
      return;
    }

    setBusyStage('선택한 도면 후보를 저장하고 있습니다...');
    setError('');

    try {
      const payload: FloorPlanSelectRequest = {
        reason: 'MOST_SIMILAR',
      };
      await apiRequest(
        `/api/v1/projects/${createdProject.id}/floor-plans/${candidateId}/select`,
        {
          method: 'POST',
          body: JSON.stringify(payload),
        },
      );

      const floorPlanResponse = await apiRequest<FloorPlanListResponse>(
        `/api/v1/projects/${createdProject.id}/floor-plans`,
      );
      setFloorPlans(floorPlanResponse.data ?? null);
      await loadProjectDetail(createdProject.id);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '도면 후보 선택에 실패했습니다.',
      );
    } finally {
      setBusyStage('');
    }
  }

  return (
    <main className="shell">
      <section className="hero-card">
        <div className="hero-copy">
          <p className="eyebrow">Phase 1.3 Vertical Slice</p>
          <h1>단지명 검색부터 평형 선택, 도면 후보 확인까지 이어집니다.</h1>
          <p className="lede">
            정확한 동과 호수를 입력하지 않고도 단지명만으로 시작할 수 있습니다.
            먼저 아파트 단지를 고르고, 그다음 평형을 선택하면 프로젝트 생성과
            도면 후보 확인으로 자연스럽게 이어집니다.
          </p>
        </div>
        <div className="hero-panel">
          <label className="field">
            <span>아파트 단지명 또는 도로명</span>
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="예: 리센츠, 헬리오시티, 마포 래미안"
            />
          </label>
          <button
            className="primary-button"
            onClick={() => void searchAddress()}
          >
            단지 검색
          </button>
          {busyStage ? <p className="status">{busyStage}</p> : null}
          {error ? <p className="error">{error}</p> : null}
        </div>
      </section>

      <section className="grid two-up">
        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">1. 단지 선택</p>
            <h2>검색 결과에서 우리 집 단지를 고르세요.</h2>
          </div>
          <div className="candidate-list">
            {searchResults.length === 0 ? (
              <p className="empty">
                단지명을 검색하면 여기에서 선택 가능한 후보가 보입니다.
              </p>
            ) : (
              searchResults.map((candidate) => (
                <button
                  key={`${candidate.roadCode}-${candidate.displayName}`}
                  className={
                    selectedCandidate?.roadCode === candidate.roadCode
                      ? 'candidate-card selected'
                      : 'candidate-card'
                  }
                  onClick={() => void chooseCandidate(candidate)}
                >
                  <strong>{candidate.displayName}</strong>
                  <span>{candidate.roadAddress}</span>
                  <small>
                    준공 {candidate.complexHint.completionYear}년 -{' '}
                    {candidate.complexHint.householdCount.toLocaleString()}세대
                  </small>
                  <small>
                    평형 후보: {candidate.complexHint.areaHints.join(', ')}㎡
                  </small>
                </button>
              ))
            )}
          </div>
        </article>

        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">2. 평형 선택</p>
            <h2>선택한 단지에 맞는 평형을 고르세요.</h2>
          </div>
          {propertySummary ? (
            <div className="summary-card">
              <h3>{propertySummary.apartmentName}</h3>
              <p>{propertySummary.roadAddress}</p>
              <div className="summary-grid">
                <span>세대 수</span>
                <strong>
                  {propertySummary.householdCount.toLocaleString()}세대
                </strong>
                <span>준공 연도</span>
                <strong>{propertySummary.completionYear}년</strong>
                <span>진행 방식</span>
                <strong>동/호 없이 평형 먼저 선택</strong>
              </div>
              <div className="candidate-list">
                {propertySummary.areaOptions.map((option) => (
                  <button
                    key={option.label}
                    className={
                      selectedAreaLabel === option.label
                        ? 'candidate-card selected'
                        : 'candidate-card'
                    }
                    onClick={() => setSelectedAreaLabel(option.label)}
                  >
                    <strong>{option.label}</strong>
                    <span>
                      방 {option.roomCount ?? '-'}개 - 욕실{' '}
                      {option.bathroomCount ?? '-'}개
                    </span>
                  </button>
                ))}
              </div>

              <label className="field">
                <span>프로젝트 제목</span>
                <input
                  value={projectTitle}
                  onChange={(event) => setProjectTitle(event.target.value)}
                />
              </label>
              <button
                className="primary-button"
                onClick={() => void createProjectFlow()}
                disabled={!canCreateProject}
              >
                이 평형으로 프로젝트 만들기
              </button>
            </div>
          ) : (
            <p className="empty">
              단지를 선택하면 여기에서 평형 버튼이 나타납니다.
            </p>
          )}
        </article>
      </section>

      <section className="grid two-up">
        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">3. 프로젝트 상태</p>
            <h2>생성된 프로젝트의 현재 상태를 확인하세요.</h2>
          </div>
          {projectDetail ? (
            <div className="summary-card">
              <h3>{projectDetail.title}</h3>
              <div className="summary-grid">
                <span>프로젝트 유형</span>
                <strong>{projectDetail.projectType}</strong>
                <span>거주 상태</span>
                <strong>{projectDetail.livingStatus}</strong>
                <span>도면 후보 수</span>
                <strong>{projectDetail.floorPlanCandidateCount}</strong>
              </div>

              {projectDetail.property ? (
                <>
                  <p className="eyebrow">Property</p>
                  <div className="summary-grid">
                    <span>단지명</span>
                    <strong>{projectDetail.property.apartmentName}</strong>
                    <span>주소</span>
                    <strong>{projectDetail.property.roadAddress}</strong>
                    <span>선택 평형</span>
                    <strong>
                      {projectDetail.property.exclusiveAreaM2
                        ? `${projectDetail.property.exclusiveAreaM2}㎡`
                        : '-'}
                    </strong>
                  </div>
                </>
              ) : null}

              {projectDetail.selectedFloorPlan ? (
                <>
                  <p className="eyebrow">Selected Floor Plan</p>
                  <div className="summary-grid">
                    <span>레이아웃</span>
                    <strong>
                      {projectDetail.selectedFloorPlan.layoutLabel}
                    </strong>
                    <span>신뢰도</span>
                    <strong>
                      {projectDetail.selectedFloorPlan.confidenceGrade} -{' '}
                      {projectDetail.selectedFloorPlan.confidenceScore.toFixed(
                        1,
                      )}
                    </strong>
                    <span>출처</span>
                    <strong>{projectDetail.selectedFloorPlan.source}</strong>
                  </div>
                </>
              ) : (
                <p className="empty">선택된 도면 후보가 아직 없습니다.</p>
              )}
            </div>
          ) : (
            <p className="empty">
              프로젝트를 만들면 여기에서 선택한 평형과 연결 상태를 확인할 수
              있습니다.
            </p>
          )}
        </article>

        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">4. 도면 후보</p>
            <h2>provider 기반 도면 후보를 비교해 보세요.</h2>
          </div>
          {floorPlans?.candidates?.length ? (
            <div className="candidate-list">
              {floorPlans.candidates.map((candidate) => (
                <div className="plan-card" key={candidate.id}>
                  <div className="plan-topline">
                    <span className="badge">{candidate.confidenceGrade}</span>
                    <span>{candidate.layoutLabel}</span>
                  </div>
                  <h3>{candidate.source}</h3>
                  <p>
                    {candidate.sourceType} - {candidate.matchType} -{' '}
                    {candidate.confidenceScore.toFixed(1)}
                  </p>
                  <p>
                    license_status: <strong>{candidate.licenseStatus}</strong>
                  </p>
                  <p>raw_payload_ref: {candidate.rawPayloadRef ?? '-'}</p>
                  <p>
                    normalized_plan_ref: {candidate.normalizedPlanRef ?? '-'}
                  </p>
                  <ul>
                    {candidate.manualCheckItems.map((item) => (
                      <li key={item}>{item}</li>
                    ))}
                  </ul>
                  <button
                    className="secondary-button"
                    onClick={() => void selectFloorPlan(candidate.id)}
                    disabled={
                      createdProject?.id == null ||
                      floorPlans.selectedPlanId === candidate.id
                    }
                  >
                    {floorPlans.selectedPlanId === candidate.id
                      ? '현재 선택된 도면'
                      : '이 도면 선택'}
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <p className="empty">
              프로젝트를 만들면 도면 후보가 여기에서 보입니다.
            </p>
          )}
        </article>
      </section>

      <section className="panel">
        <div className="section-head">
          <p className="eyebrow">Repository Snapshot</p>
          <h2>현재 생성된 프로젝트</h2>
        </div>
        <div className="project-list">
          {projects.length === 0 ? (
            <p className="empty">아직 생성된 프로젝트가 없습니다.</p>
          ) : (
            projects.map((project) => (
              <div className="project-card" key={project.id}>
                <strong>{project.title}</strong>
                <span>
                  {project.projectType} - {project.livingStatus}
                </span>
                <small>
                  propertyAttached:{' '}
                  {project.propertyAttached ? 'true' : 'false'}
                </small>
                <Link
                  className="link-button inline-link"
                  href={`/projects/${project.id}/home`}
                >
                  프로젝트 홈 보기
                </Link>
              </div>
            ))
          )}
          {createdProject ? (
            <div className="stack">
              <p className="status">
                최근 생성 프로젝트: {createdProject.title} ({createdProject.id})
              </p>
              <Link
                className="link-button inline-link"
                href={`/projects/${createdProject.id}/home`}
              >
                최근 프로젝트로 이동
              </Link>
            </div>
          ) : null}
        </div>
      </section>
    </main>
  );
}
