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
  const [query, setQuery] = useState('recents');
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
  const [projectTitle, setProjectTitle] = useState('My interior project');
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
    setBusyStage('Searching apartment complexes...');
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
          : 'Complex search failed.',
      );
    } finally {
      setBusyStage('');
    }
  }

  async function chooseCandidate(candidate: AddressSearchCandidate) {
    setBusyStage('Loading area options for the selected complex...');
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
        setProjectTitle(`${summary.apartmentName} interior`);
      }
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Area options could not be loaded.',
      );
    } finally {
      setBusyStage('');
    }
  }

  async function createProjectFlow() {
    if (!selectedCandidate || !propertySummary || !selectedArea) {
      return;
    }

    setBusyStage('Creating project and loading floor plan candidates...');
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
        throw new Error('Project create response is empty.');
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
          : 'Project flow failed.',
      );
    } finally {
      setBusyStage('');
    }
  }

  async function selectFloorPlan(candidateId: string) {
    if (!createdProject) {
      return;
    }

    setBusyStage('Saving selected floor plan...');
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
          : 'Floor plan selection failed.',
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
          <h1>Search by complex, pick an area, then keep moving.</h1>
          <p className="lede">
            The onboarding flow no longer asks for exact dong and ho. Pick the
            apartment complex first, select one of the available area options,
            and continue into project creation and floor plan review.
          </p>
        </div>
        <div className="hero-panel">
          <label className="field">
            <span>Apartment complex or road name</span>
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Examples: recents, helio city, mapo raemian"
            />
          </label>
          <button
            className="primary-button"
            onClick={() => void searchAddress()}
          >
            Search complexes
          </button>
          {busyStage ? <p className="status">{busyStage}</p> : null}
          {error ? <p className="error">{error}</p> : null}
        </div>
      </section>

      <section className="grid two-up">
        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">1. Complex Search</p>
            <h2>Choose a complex</h2>
          </div>
          <div className="candidate-list">
            {searchResults.length === 0 ? (
              <p className="empty">
                Search for a complex name to see selectable candidates here.
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
                    Built {candidate.complexHint.completionYear} -{' '}
                    {candidate.complexHint.householdCount.toLocaleString()}{' '}
                    homes
                  </small>
                  <small>
                    Area hints: {candidate.complexHint.areaHints.join(', ')} m2
                  </small>
                </button>
              ))
            )}
          </div>
        </article>

        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">2. Area Selection</p>
            <h2>Pick the matching area</h2>
          </div>
          {propertySummary ? (
            <div className="summary-card">
              <h3>{propertySummary.apartmentName}</h3>
              <p>{propertySummary.roadAddress}</p>
              <div className="summary-grid">
                <span>Households</span>
                <strong>
                  {propertySummary.householdCount.toLocaleString()}
                </strong>
                <span>Completion year</span>
                <strong>{propertySummary.completionYear}</strong>
                <span>Default mode</span>
                <strong>Complex first, no unit number</strong>
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
                      Rooms {option.roomCount ?? '-'} - Baths{' '}
                      {option.bathroomCount ?? '-'}
                    </span>
                  </button>
                ))}
              </div>

              <label className="field">
                <span>Project title</span>
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
                Create project with this area
              </button>
            </div>
          ) : (
            <p className="empty">
              Choose a complex and the area buttons will appear here.
            </p>
          )}
        </article>
      </section>

      <section className="grid two-up">
        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">3. Project Snapshot</p>
            <h2>Current project state</h2>
          </div>
          {projectDetail ? (
            <div className="summary-card">
              <h3>{projectDetail.title}</h3>
              <div className="summary-grid">
                <span>Project type</span>
                <strong>{projectDetail.projectType}</strong>
                <span>Living status</span>
                <strong>{projectDetail.livingStatus}</strong>
                <span>Floor plan candidates</span>
                <strong>{projectDetail.floorPlanCandidateCount}</strong>
              </div>

              {projectDetail.property ? (
                <>
                  <p className="eyebrow">Property</p>
                  <div className="summary-grid">
                    <span>Complex</span>
                    <strong>{projectDetail.property.apartmentName}</strong>
                    <span>Address</span>
                    <strong>{projectDetail.property.roadAddress}</strong>
                    <span>Selected area</span>
                    <strong>
                      {projectDetail.property.exclusiveAreaM2
                        ? `${projectDetail.property.exclusiveAreaM2} m2`
                        : '-'}
                    </strong>
                  </div>
                </>
              ) : null}

              {projectDetail.selectedFloorPlan ? (
                <>
                  <p className="eyebrow">Selected Floor Plan</p>
                  <div className="summary-grid">
                    <span>Layout</span>
                    <strong>
                      {projectDetail.selectedFloorPlan.layoutLabel}
                    </strong>
                    <span>Confidence</span>
                    <strong>
                      {projectDetail.selectedFloorPlan.confidenceGrade} -{' '}
                      {projectDetail.selectedFloorPlan.confidenceScore.toFixed(
                        1,
                      )}
                    </strong>
                    <span>Source</span>
                    <strong>{projectDetail.selectedFloorPlan.source}</strong>
                  </div>
                </>
              ) : (
                <p className="empty">No floor plan has been selected yet.</p>
              )}
            </div>
          ) : (
            <p className="empty">
              Project details will appear here after the first area-based
              project is created.
            </p>
          )}
        </article>

        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">4. Floor Plan Candidates</p>
            <h2>Provider-based floor plan results</h2>
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
                      ? 'Current selection'
                      : 'Select this plan'}
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <p className="empty">
              Floor plan candidates will appear after project creation.
            </p>
          )}
        </article>
      </section>

      <section className="panel">
        <div className="section-head">
          <p className="eyebrow">Repository Snapshot</p>
          <h2>Created projects</h2>
        </div>
        <div className="project-list">
          {projects.length === 0 ? (
            <p className="empty">No projects have been created yet.</p>
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
                  Open project home
                </Link>
              </div>
            ))
          )}
          {createdProject ? (
            <div className="stack">
              <p className="status">
                Latest project: {createdProject.title} ({createdProject.id})
              </p>
              <Link
                className="link-button inline-link"
                href={`/projects/${createdProject.id}/home`}
              >
                Go to latest project
              </Link>
            </div>
          ) : null}
        </div>
      </section>
    </main>
  );
}
