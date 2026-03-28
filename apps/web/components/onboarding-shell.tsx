'use client';

import type {
  AddressSearchCandidate,
  AddressSearchRequest,
  AddressSearchResponse,
  AttachPropertyRequest,
  CreateProjectRequest,
  CreateProjectResponse,
  DetailOptionsRequest,
  DetailOptionsResponse,
  FloorPlanListResponse,
  FloorPlanResolveResponse,
  FloorPlanSelectRequest,
  ProjectDetail,
  ProjectSummary,
  PropertyResolveRequest,
  PropertyResolveResponse,
} from '@selfinterior/shared-types';
import Link from 'next/link';
import { startTransition, useEffect, useMemo, useState } from 'react';
import { apiRequest } from '@/lib/api';

export function OnboardingShell() {
  const [query, setQuery] = useState('잠실 리센츠 201동 1203호');
  const [searchResults, setSearchResults] = useState<AddressSearchCandidate[]>(
    [],
  );
  const [selectedCandidate, setSelectedCandidate] =
    useState<AddressSearchCandidate | null>(null);
  const [dongOptions, setDongOptions] = useState<string[]>([]);
  const [hoOptions, setHoOptions] = useState<string[]>([]);
  const [selectedDong, setSelectedDong] = useState('');
  const [selectedHo, setSelectedHo] = useState('1203');
  const [propertySummary, setPropertySummary] = useState<
    PropertyResolveResponse['propertySummary'] | null
  >(null);
  const [externalRefs, setExternalRefs] = useState<
    PropertyResolveResponse['externalRefs']
  >([]);
  const [projectTitle, setProjectTitle] = useState('잠실 리센츠 인테리어');
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

  useEffect(() => {
    startTransition(() => {
      void loadProjects();
    });
  }, []);

  const canResolveProperty = useMemo(
    () => Boolean(selectedCandidate && selectedDong && selectedHo),
    [selectedCandidate, selectedDong, selectedHo],
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
    setBusyStage('주소 후보를 찾는 중입니다.');
    setError('');
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
      setSelectedCandidate(null);
      setPropertySummary(null);
      setExternalRefs([]);
      setCreatedProject(null);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '주소 검색에 실패했습니다.',
      );
    } finally {
      setBusyStage('');
    }
  }

  async function chooseCandidate(candidate: AddressSearchCandidate) {
    setSelectedCandidate(candidate);
    setSelectedDong(candidate.dongCandidates[0] ?? '');
    setError('');

    try {
      const detailRequest: DetailOptionsRequest = {
        roadCode: candidate.roadCode,
        buildingMainNo: candidate.buildingMainNo,
        buildingSubNo: candidate.buildingSubNo,
        queryType: 'dong',
      };
      const response = await apiRequest<DetailOptionsResponse>(
        '/api/v1/address/detail-options',
        {
          method: 'POST',
          body: JSON.stringify(detailRequest),
        },
      );
      setDongOptions(response.data?.options ?? candidate.dongCandidates);
      setSelectedDong(
        response.data?.options?.[0] ?? candidate.dongCandidates[0] ?? '',
      );

      const hoResponse = await apiRequest<DetailOptionsResponse>(
        '/api/v1/address/detail-options',
        {
          method: 'POST',
          body: JSON.stringify({ ...detailRequest, queryType: 'ho' }),
        },
      );
      setHoOptions(hoResponse.data?.options ?? []);
      setSelectedHo(hoResponse.data?.options?.[0] ?? selectedHo);
    } catch {
      setDongOptions(candidate.dongCandidates);
      setHoOptions([]);
    }
  }

  async function resolveProperty() {
    if (!selectedCandidate) {
      return;
    }

    setBusyStage('집 정보를 정리하는 중입니다.');
    setError('');
    try {
      const payload: PropertyResolveRequest = {
        roadAddress: selectedCandidate.roadAddress,
        dongNo: selectedDong,
        hoNo: selectedHo,
      };
      const response = await apiRequest<PropertyResolveResponse>(
        '/api/v1/property/resolve',
        {
          method: 'POST',
          body: JSON.stringify(payload),
        },
      );
      setPropertySummary(response.data?.propertySummary ?? null);
      setExternalRefs(response.data?.externalRefs ?? []);
      if (response.data?.propertySummary?.apartmentName) {
        setProjectTitle(
          `${response.data.propertySummary.apartmentName} 인테리어`,
        );
      }
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '집 정보 정리에 실패했습니다.',
      );
    } finally {
      setBusyStage('');
    }
  }

  async function createProjectFlow() {
    if (!selectedCandidate || !propertySummary) {
      return;
    }

    setBusyStage('프로젝트와 도면 후보를 준비하는 중입니다.');
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
        dongNo: propertySummary.dongNo,
        hoNo: propertySummary.hoNo,
        exclusiveAreaM2: propertySummary.exclusiveAreaCandidates[0],
        roomCount: propertySummary.roomCountCandidates[0],
        bathroomCount: propertySummary.bathroomCountCandidates[0],
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

    setBusyStage('선택한 도면 후보를 프로젝트에 반영하는 중입니다.');
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
          <p className="eyebrow">Phase 1.2 Vertical Slice</p>
          <h1>주소부터 선택된 도면 후보까지 한 번에 이어지는 흐름</h1>
          <p className="lede">
            주소 검색, 집 정보 resolve, 프로젝트 생성, provider 기반 도면 후보
            저장과 수동 선택, 프로젝트 홈 진입까지 한 화면에서 확인할 수 있게
            구성했습니다.
          </p>
        </div>
        <div className="hero-panel">
          <label className="field">
            <span>주소 또는 단지명</span>
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="예: 잠실 리센츠 201동 1203호"
            />
          </label>
          <button
            className="primary-button"
            onClick={() => void searchAddress()}
          >
            주소 검색 시작
          </button>
          {busyStage ? <p className="status">{busyStage}</p> : null}
          {error ? <p className="error">{error}</p> : null}
        </div>
      </section>

      <section className="grid two-up">
        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">1. Address Resolution</p>
            <h2>검색 결과와 동·호 선택</h2>
          </div>
          <div className="candidate-list">
            {searchResults.length === 0 ? (
              <p className="empty">
                아직 검색 결과가 없습니다. 주소를 검색하면 후보가 여기에
                표시됩니다.
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
                    준공 {candidate.complexHint.completionYear}년 ·{' '}
                    {candidate.complexHint.householdCount.toLocaleString()}세대
                  </small>
                </button>
              ))
            )}
          </div>
          {selectedCandidate ? (
            <div className="option-box">
              <label className="field">
                <span>동 선택</span>
                <select
                  value={selectedDong}
                  onChange={(event) => setSelectedDong(event.target.value)}
                >
                  {dongOptions.map((option) => (
                    <option key={option} value={option}>
                      {option}동
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>호 선택</span>
                <input
                  list="ho-options"
                  value={selectedHo}
                  onChange={(event) => setSelectedHo(event.target.value)}
                />
                <datalist id="ho-options">
                  {hoOptions.map((option) => (
                    <option key={option} value={option} />
                  ))}
                </datalist>
              </label>
              <button
                className="secondary-button"
                onClick={() => void resolveProperty()}
                disabled={!canResolveProperty}
              >
                집 정보 정리
              </button>
            </div>
          ) : null}
        </article>

        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">2. Property + Project</p>
            <h2>집 정보 요약과 프로젝트 생성</h2>
          </div>
          {propertySummary ? (
            <div className="summary-card">
              <h3>{propertySummary.apartmentName}</h3>
              <p>{propertySummary.roadAddress}</p>
              <div className="summary-grid">
                <span>전용면적 후보</span>
                <strong>
                  {propertySummary.exclusiveAreaCandidates.join(', ')}㎡
                </strong>
                <span>방/욕실</span>
                <strong>
                  {propertySummary.roomCountCandidates[0]} /{' '}
                  {propertySummary.bathroomCountCandidates[0]}
                </strong>
                <span>동·호</span>
                <strong>
                  {propertySummary.dongNo}동 {propertySummary.hoNo}호
                </strong>
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
              >
                이 집으로 프로젝트 만들기
              </button>
            </div>
          ) : (
            <p className="empty">
              주소 후보를 선택하고 집 정보를 정리하면 프로젝트 생성 단계가
              열립니다.
            </p>
          )}
        </article>
      </section>

      <section className="grid two-up">
        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">3. Project Snapshot</p>
            <h2>생성된 프로젝트의 현재 상태</h2>
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
                    <span>동·호</span>
                    <strong>
                      {projectDetail.property.dongNo}동{' '}
                      {projectDetail.property.hoNo}호
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
                      {projectDetail.selectedFloorPlan.confidenceGrade} ·{' '}
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
              프로젝트를 생성하면 상세 상태가 여기 표시됩니다.
            </p>
          )}
        </article>

        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">4. Floor Plan Candidates</p>
            <h2>provider 기반 도면 후보 저장 결과</h2>
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
                    {candidate.sourceType} · {candidate.matchType} ·{' '}
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
                      ? '현재 선택된 후보'
                      : '이 후보 선택'}
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <p className="empty">
              프로젝트를 생성하면 도면 후보가 여기 표시됩니다.
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
                  {project.projectType} · {project.livingStatus}
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
                생성된 프로젝트 홈으로 이동
              </Link>
            </div>
          ) : null}
        </div>
      </section>
    </main>
  );
}
