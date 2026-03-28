'use client';

import type {
  ProcessPlanResponse,
  ProcessPlanStepDetailResponse,
  ProcessStepSummary,
  ProcessTask,
  ProcessTaskToggleRequest,
} from '@selfinterior/shared-types';
import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { ApiRequestError, apiRequest } from '@/lib/api';

type LoadState = 'idle' | 'loading' | 'ready' | 'missing' | 'error';

function statusTone(status: ProcessStepSummary['status']) {
  switch (status) {
    case 'DONE':
      return 'tone-done';
    case 'IN_PROGRESS':
      return 'tone-ready';
    default:
      return 'tone-blocked';
  }
}

export function ProcessPlanScreen({ projectId }: { projectId: string }) {
  const [state, setState] = useState<LoadState>('idle');
  const [busy, setBusy] = useState('');
  const [error, setError] = useState('');
  const [plan, setPlan] = useState<ProcessPlanResponse | null>(null);
  const [selectedStepKey, setSelectedStepKey] = useState('');
  const [stepDetail, setStepDetail] =
    useState<ProcessPlanStepDetailResponse | null>(null);

  const loadPlan = useCallback(async () => {
    setState('loading');
    setError('');

    try {
      const response = await apiRequest<ProcessPlanResponse>(
        `/api/v1/projects/${projectId}/process-plan`,
      );
      const nextPlan = response.data ?? null;
      setPlan(nextPlan);
      setSelectedStepKey(
        nextPlan?.currentStepKey ?? nextPlan?.steps?.[0]?.stepKey ?? '',
      );
      setState(nextPlan ? 'ready' : 'missing');
    } catch (caughtError) {
      if (
        caughtError instanceof ApiRequestError &&
        caughtError.status === 404
      ) {
        setPlan(null);
        setSelectedStepKey('');
        setState('missing');
        return;
      }

      setState('error');
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '공정 플랜을 불러오지 못했습니다.',
      );
    }
  }, [projectId]);

  const loadStep = useCallback(async (stepKey: string) => {
    try {
      const response = await apiRequest<ProcessPlanStepDetailResponse>(
        `/api/v1/projects/${projectId}/process-plan/steps/${stepKey}`,
      );
      setStepDetail(response.data ?? null);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '공정 단계 상세를 불러오지 못했습니다.',
      );
    }
  }, [projectId]);

  useEffect(() => {
    void loadPlan();
  }, [loadPlan]);

  useEffect(() => {
    if (!plan?.steps?.length) {
      setStepDetail(null);
      return;
    }

    const nextStepKey =
      selectedStepKey || plan.currentStepKey || plan.steps[0]?.stepKey || '';
    if (!nextStepKey) {
      return;
    }
    if (nextStepKey !== selectedStepKey) {
      setSelectedStepKey(nextStepKey);
      return;
    }

    void loadStep(nextStepKey);
  }, [loadStep, plan, selectedStepKey]);

  async function generatePlan() {
    setBusy('공정 플랜을 생성하는 중입니다.');
    setError('');
    try {
      await apiRequest<ProcessPlanResponse>(
        `/api/v1/projects/${projectId}/process-plan/generate`,
        {
          method: 'POST',
          body: JSON.stringify({}),
        },
      );
      await loadPlan();
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '공정 플랜 생성에 실패했습니다.',
      );
    } finally {
      setBusy('');
    }
  }

  async function toggleTask(task: ProcessTask) {
    setBusy('체크리스트 상태를 저장하는 중입니다.');
    setError('');
    try {
      const payload: ProcessTaskToggleRequest = {
        completed: !task.completed,
      };
      await apiRequest(
        `/api/v1/projects/${projectId}/process-plan/tasks/${task.id}`,
        {
          method: 'PATCH',
          body: JSON.stringify(payload),
        },
      );
      await loadPlan();
      if (selectedStepKey) {
        await loadStep(selectedStepKey);
      }
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '체크리스트 저장에 실패했습니다.',
      );
    } finally {
      setBusy('');
    }
  }

  return (
    <main className="shell">
      <section className="hero-card">
        <div className="hero-copy">
          <p className="eyebrow">Process Plan</p>
          <h1>도면 기준으로 공정 순서를 정리하는 실행 플래너</h1>
          <p className="lede">
            프로젝트 범위와 거주 상태를 반영해 단계별 체크리스트를 만들고, 현재
            공정 상태를 계속 갱신합니다.
          </p>
          <div className="stack-inline">
            <Link
              className="link-button inline-link"
              href={`/projects/${projectId}/home`}
            >
              프로젝트 홈으로 돌아가기
            </Link>
            <button
              className="primary-button inline-link"
              onClick={() => void generatePlan()}
            >
              {plan ? '공정 플랜 다시 불러오기' : '공정 플랜 생성'}
            </button>
          </div>
        </div>
        <div className="hero-panel">
          {busy ? <p className="status">{busy}</p> : null}
          {error ? <p className="error">{error}</p> : null}
          {plan ? (
            <div className="summary-grid">
              <span>플랜 상태</span>
              <strong>{plan.status}</strong>
              <span>현재 단계</span>
              <strong>{plan.currentStepKey ?? '미정'}</strong>
              <span>진행률</span>
              <strong>{plan.progressPercent}%</strong>
            </div>
          ) : null}
        </div>
      </section>

      {state === 'missing' ? (
        <section className="panel">
          <div className="section-head">
            <p className="eyebrow">공정 플랜 준비</p>
            <h2>아직 생성된 공정 플랜이 없습니다.</h2>
          </div>
          <p className="empty">
            선택된 도면 후보를 기준으로 프로젝트별 공정 초안을 생성할 수
            있습니다. 도면 후보가 없으면 먼저 프로젝트 홈에서 도면을 확정해야
            합니다.
          </p>
        </section>
      ) : null}

      {state === 'ready' && plan ? (
        <section className="grid two-up">
          <article className="panel">
            <div className="section-head">
              <p className="eyebrow">공정 타임라인</p>
              <h2>프로젝트별 단계 목록</h2>
            </div>
            <div className="task-list">
              {plan.steps.map((step) => (
                <button
                  className={`task-card ${statusTone(step.status)} ${
                    selectedStepKey === step.stepKey ? 'selected-outline' : ''
                  }`}
                  key={step.stepKey}
                  onClick={() => setSelectedStepKey(step.stepKey)}
                >
                  <div className="plan-topline">
                    <strong>{step.title}</strong>
                    <span className="badge">{step.status}</span>
                  </div>
                  <p>
                    {step.completedTaskCount}/{step.totalTaskCount} 완료 · 예상{' '}
                    {step.durationDays}일
                  </p>
                </button>
              ))}
            </div>
          </article>

          <article className="panel">
            <div className="section-head">
              <p className="eyebrow">단계 상세</p>
              <h2>{stepDetail?.step.title ?? '단계를 선택하세요'}</h2>
            </div>
            {stepDetail ? (
              <div className="summary-card">
                <p>{stepDetail.purposeText}</p>
                <div className="stack">
                  <div className="detail-block">
                    <strong>시작 전</strong>
                    <p>{stepDetail.startCheckIntro}</p>
                  </div>
                  <div className="detail-block">
                    <strong>결정 포인트</strong>
                    <ul>
                      {stepDetail.decisionPoints.map((point) => (
                        <li key={point}>{point}</li>
                      ))}
                    </ul>
                  </div>
                  <div className="detail-block">
                    <strong>셀프로 가능한 범위</strong>
                    <p>{stepDetail.selfWorkText}</p>
                  </div>
                  <div className="detail-block">
                    <strong>전문가 필요 범위</strong>
                    <p>{stepDetail.expertRequiredText}</p>
                  </div>
                  <div className="detail-block">
                    <strong>실수 방지</strong>
                    <p>{stepDetail.mistakesText}</p>
                  </div>
                  <div className="detail-block">
                    <strong>다음 단계 전 확인</strong>
                    <ul>
                      {stepDetail.nextStepChecks.map((item) => (
                        <li key={item}>{item}</li>
                      ))}
                    </ul>
                  </div>
                </div>
                <div className="detail-block">
                  <strong>체크리스트</strong>
                  <div className="task-list">
                    {stepDetail.tasks.map((task) => (
                      <label className="check-row" key={task.id}>
                        <input
                          checked={task.completed}
                          onChange={() => void toggleTask(task)}
                          type="checkbox"
                        />
                        <div>
                          <span>{task.title}</span>
                          {task.description ? (
                            <small>{task.description}</small>
                          ) : null}
                        </div>
                      </label>
                    ))}
                  </div>
                </div>
              </div>
            ) : (
              <p className="empty">단계 상세를 불러오는 중입니다.</p>
            )}
          </article>
        </section>
      ) : null}
    </main>
  );
}
