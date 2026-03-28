'use client';

import type {
  ProjectHomeAction,
  ProjectHomePlaceholderCard,
  ProjectHomeResponse,
} from '@selfinterior/shared-types';
import Link from 'next/link';
import { useEffect, useState } from 'react';
import { apiRequest } from '@/lib/api';

type LoadState = 'idle' | 'loading' | 'ready' | 'error';

function actionTone(status: ProjectHomeAction['status']) {
  switch (status) {
    case 'DONE':
      return 'tone-done';
    case 'READY':
      return 'tone-ready';
    case 'UPCOMING':
      return 'tone-upcoming';
    default:
      return 'tone-blocked';
  }
}

function PlaceholderCard({ card }: { card: ProjectHomePlaceholderCard }) {
  return (
    <article className="placeholder-card">
      <div className="plan-topline">
        <p className="eyebrow">{card.title}</p>
        <span className="badge">{card.status}</span>
      </div>
      <p>{card.description}</p>
      <Link className="link-button" href={card.primaryActionPath}>
        {card.primaryActionLabel}
      </Link>
    </article>
  );
}

export function ProjectHomeScreen({ projectId }: { projectId: string }) {
  const [home, setHome] = useState<ProjectHomeResponse | null>(null);
  const [state, setState] = useState<LoadState>('idle');
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setState('loading');
      setError('');

      try {
        const response = await apiRequest<ProjectHomeResponse>(
          `/api/v1/projects/${projectId}/home`,
        );
        if (!cancelled) {
          setHome(response.data ?? null);
          setState('ready');
        }
      } catch (caughtError) {
        if (!cancelled) {
          setError(
            caughtError instanceof Error
              ? caughtError.message
              : '프로젝트 홈을 불러오지 못했습니다.',
          );
          setState('error');
        }
      }
    }

    void load();

    return () => {
      cancelled = true;
    };
  }, [projectId]);

  return (
    <main className="shell">
      <section className="hero-card">
        <div className="hero-copy">
          <p className="eyebrow">Project Home</p>
          <h1>지금 해야 할 일을 한 화면에서 보는 프로젝트 홈</h1>
          <p className="lede">
            주소와 도면을 기준으로 우리 집 요약, 다음 액션, 후속 모듈 연결
            지점을 한 번에 확인합니다.
          </p>
          <Link className="link-button inline-link" href="/">
            온보딩 화면으로 돌아가기
          </Link>
        </div>
        <div className="hero-panel">
          {state === 'loading' ? (
            <p className="status">프로젝트 홈을 불러오는 중입니다.</p>
          ) : null}
          {state === 'error' ? <p className="error">{error}</p> : null}
          {home ? (
            <div className="summary-grid">
              <span>프로젝트</span>
              <strong>{home.project.title}</strong>
              <span>유형</span>
              <strong>{home.project.projectType}</strong>
              <span>거주 상태</span>
              <strong>{home.project.livingStatus}</strong>
              <span>현재 단계</span>
              <strong>
                {home.project.currentProcessStep ?? '초기 온보딩'}
              </strong>
            </div>
          ) : null}
        </div>
      </section>

      {home ? (
        <>
          <section className="grid two-up">
            <article className="panel" id="property">
              <div className="section-head">
                <p className="eyebrow">우리 집 요약</p>
                <h2>주소와 집 기본 정보</h2>
              </div>
              {home.property ? (
                <div className="summary-card">
                  <h3>{home.property.apartmentName}</h3>
                  <p>{home.property.roadAddress}</p>
                  <div className="summary-grid">
                    <span>동/호</span>
                    <strong>
                      {home.property.dongNo ?? '-'}동{' '}
                      {home.property.hoNo ?? '-'}호
                    </strong>
                    <span>준공연도</span>
                    <strong>{home.property.completionYear ?? '-'}</strong>
                    <span>세대수</span>
                    <strong>
                      {home.property.householdCount?.toLocaleString() ?? '-'}
                    </strong>
                    <span>전용면적</span>
                    <strong>{home.property.exclusiveAreaM2 ?? '-'}㎡</strong>
                  </div>
                </div>
              ) : (
                <p className="empty">집 정보가 아직 연결되지 않았습니다.</p>
              )}
            </article>

            <article className="panel" id="plan">
              <div className="section-head">
                <p className="eyebrow">도면 요약</p>
                <h2>현재 기준 구조</h2>
              </div>
              {home.floorPlan ? (
                <div className="summary-card">
                  <div className="plan-topline">
                    <h3>{home.floorPlan.layoutLabel}</h3>
                    <span className="badge">
                      {home.floorPlan.confidenceGrade}
                    </span>
                  </div>
                  <p>{home.floorPlan.structureSummary}</p>
                  <div className="summary-grid">
                    <span>출처</span>
                    <strong>{home.floorPlan.source}</strong>
                    <span>라이선스</span>
                    <strong>{home.floorPlan.licenseStatus}</strong>
                    <span>후보 수</span>
                    <strong>{home.floorPlan.candidateCount}</strong>
                  </div>
                  {home.floorPlan.manualCheckItems.length > 0 ? (
                    <ul>
                      {home.floorPlan.manualCheckItems.map((item) => (
                        <li key={item}>{item}</li>
                      ))}
                    </ul>
                  ) : (
                    <p className="status">추가 실측 확인 항목이 없습니다.</p>
                  )}
                </div>
              ) : (
                <p className="empty">선택된 도면 후보가 아직 없습니다.</p>
              )}
            </article>
          </section>

          <section className="grid two-up">
            <article className="panel" id="process">
              <div className="section-head">
                <p className="eyebrow">지금 해야 할 일</p>
                <h2>다음 액션</h2>
              </div>
              <div className="task-list">
                {home.nextActions.map((action) => (
                  <div
                    className={`task-card ${actionTone(action.status)}`}
                    key={action.key}
                  >
                    <div className="plan-topline">
                      <strong>{action.title}</strong>
                      <span className="badge">{action.status}</span>
                    </div>
                    <p>{action.description}</p>
                    <Link className="link-button" href={action.path}>
                      관련 화면 열기
                    </Link>
                  </div>
                ))}
              </div>
            </article>

            <article className="panel">
              <div className="section-head">
                <p className="eyebrow">후속 모듈</p>
                <h2>곧 연결될 카드</h2>
              </div>
              <div className="stack">
                <div id="recent-questions">
                  <PlaceholderCard card={home.recentQuestions} />
                </div>
                <div id="recommended-experts">
                  <PlaceholderCard card={home.recommendedExperts} />
                </div>
              </div>
            </article>
          </section>
        </>
      ) : null}
    </main>
  );
}
