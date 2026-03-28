'use client';

import type {
  CreateExpertLeadResponse,
  ExpertCategoryListResponse,
  ExpertDetailResponse,
  ExpertRecommendationResponse,
  ExpertSummary,
} from '@selfinterior/shared-types';
import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { apiRequest } from '@/lib/api';

export function ExpertLeadScreen({ projectId }: { projectId: string }) {
  const [busy, setBusy] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [recommended, setRecommended] =
    useState<ExpertRecommendationResponse | null>(null);
  const [selectedExpertId, setSelectedExpertId] = useState('');
  const [detail, setDetail] = useState<ExpertDetailResponse | null>(null);
  const [requestedCategoryKey, setRequestedCategoryKey] = useState('');
  const [budgetMin, setBudgetMin] = useState('');
  const [budgetMax, setBudgetMax] = useState('');
  const [desiredStartDate, setDesiredStartDate] = useState('');
  const [message, setMessage] = useState(
    '현재 집 상태를 먼저 점검받고 필요한 부분 시공 범위를 상담하고 싶습니다.',
  );
  const [categoryNameMap, setCategoryNameMap] = useState<
    Record<string, string>
  >({});

  const loadRecommendations = useCallback(async () => {
    const [recommendationResponse, categoryResponse] = await Promise.all([
      apiRequest<ExpertRecommendationResponse>(
        `/api/v1/projects/${projectId}/expert-recommendations`,
      ),
      apiRequest<ExpertCategoryListResponse>('/api/v1/expert-categories'),
    ]);

    const recommendation = recommendationResponse.data ?? null;
    setRecommended(recommendation);
    setSelectedExpertId(recommendation?.experts[0]?.id ?? '');
    setRequestedCategoryKey(recommendation?.primaryCategoryKey ?? '');
    setCategoryNameMap(
      Object.fromEntries(
        (categoryResponse.data?.categories ?? []).map((category) => [
          category.key,
          category.name,
        ]),
      ),
    );
  }, [projectId]);

  const loadDetail = useCallback(async () => {
    if (!selectedExpertId) {
      setDetail(null);
      return;
    }
    const response = await apiRequest<ExpertDetailResponse>(
      `/api/v1/experts/${selectedExpertId}`,
    );
    setDetail(response.data ?? null);
  }, [selectedExpertId]);

  useEffect(() => {
    async function load() {
      setError('');
      try {
        await loadRecommendations();
      } catch (caughtError) {
        setError(
          caughtError instanceof Error
            ? caughtError.message
            : '전문가 추천을 불러오지 못했습니다.',
        );
      }
    }

    void load();
  }, [loadRecommendations]);

  useEffect(() => {
    async function load() {
      try {
        await loadDetail();
      } catch (caughtError) {
        setError(
          caughtError instanceof Error
            ? caughtError.message
            : '전문가 상세를 불러오지 못했습니다.',
        );
      }
    }

    void load();
  }, [loadDetail]);

  async function submitLead() {
    if (!selectedExpertId || !requestedCategoryKey) {
      setError('전문가와 요청 카테고리를 먼저 확인하세요.');
      return;
    }

    setBusy('전문가 문의를 등록하는 중입니다.');
    setError('');
    setSuccess('');

    try {
      const response = await apiRequest<CreateExpertLeadResponse>(
        `/api/v1/projects/${projectId}/expert-leads`,
        {
          method: 'POST',
          body: JSON.stringify({
            expertId: selectedExpertId,
            requestedCategoryKey,
            budgetMin: budgetMin ? Number(budgetMin) : undefined,
            budgetMax: budgetMax ? Number(budgetMax) : undefined,
            desiredStartDate: desiredStartDate || undefined,
            message,
          }),
        },
      );
      setSuccess(
        `문의가 저장되었습니다. leadId=${response.data?.leadId ?? 'unknown'}`,
      );
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '전문가 문의 등록에 실패했습니다.',
      );
    } finally {
      setBusy('');
    }
  }

  const selectedExpert: ExpertSummary | undefined = recommended?.experts.find(
    (expert) => expert.id === selectedExpertId,
  );

  return (
    <main className="shell">
      <section className="hero-card">
        <div className="hero-copy">
          <p className="eyebrow">Expert Lead</p>
          <h1>현재 공정과 최근 질문 위험도를 반영한 전문가 추천과 문의</h1>
          <p className="lede">
            주소 지역, 예산, 질문 위험도를 반영해 추천 전문가를 보여주고
            프로젝트 요약을 포함한 문의를 생성합니다.
          </p>
          <div className="stack-inline">
            <Link
              className="link-button inline-link"
              href={`/projects/${projectId}/home`}
            >
              프로젝트 홈으로 돌아가기
            </Link>
            <Link
              className="link-button inline-link"
              href={`/projects/${projectId}/qa`}
            >
              질문 화면 보기
            </Link>
            <Link
              className="link-button inline-link"
              href={`/projects/${projectId}/style`}
            >
              스타일 페이지 보기
            </Link>
          </div>
        </div>
        <div className="hero-panel">
          {busy ? <p className="status">{busy}</p> : null}
          {error ? <p className="error">{error}</p> : null}
          {success ? <p className="status">{success}</p> : null}
          <div className="summary-grid">
            <span>우선 카테고리</span>
            <strong>{recommended?.primaryCategoryName ?? '미지정'}</strong>
            <span>보조 카테고리</span>
            <strong>{recommended?.secondaryCategoryName ?? '없음'}</strong>
            <span>추천 수</span>
            <strong>{recommended?.experts.length ?? 0}</strong>
          </div>
          {recommended?.rationale ? <p>{recommended.rationale}</p> : null}
        </div>
      </section>

      <section className="grid two-up">
        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">추천 전문가</p>
            <h2>현재 프로젝트 기준 추천 목록</h2>
          </div>
          <div className="task-list">
            {recommended?.experts.length ? (
              recommended.experts.map((expert) => (
                <button
                  className={`task-card tone-ready ${
                    selectedExpertId === expert.id ? 'selected-outline' : ''
                  }`}
                  key={expert.id}
                  onClick={() => {
                    setSelectedExpertId(expert.id);
                    setRequestedCategoryKey(
                      expert.categoryKeys.includes(
                        recommended?.primaryCategoryKey ?? '',
                      )
                        ? (recommended?.primaryCategoryKey ?? '')
                        : (expert.categoryKeys[0] ?? ''),
                    );
                  }}
                >
                  <div className="plan-topline">
                    <strong>{expert.companyName}</strong>
                    <span className="badge">
                      {expert.recommendationScore?.toFixed(1) ?? '-'}
                    </span>
                  </div>
                  <p>{expert.recommendationReason ?? expert.introText}</p>
                  <small>{expert.categoryNames.join(', ')}</small>
                </button>
              ))
            ) : (
              <p className="empty">현재 추천 가능한 전문가가 없습니다.</p>
            )}
          </div>
        </article>

        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">전문가 상세</p>
            <h2>{detail?.expert.companyName ?? '전문가를 선택하세요'}</h2>
          </div>
          {detail ? (
            <div className="summary-card">
              <p>{detail.expert.introText}</p>
              <div className="summary-grid">
                <span>담당자</span>
                <strong>{detail.expert.contactName}</strong>
                <span>예산 범위</span>
                <strong>
                  {detail.expert.minBudget ?? '-'} ~{' '}
                  {detail.expert.maxBudget ?? '-'}
                </strong>
                <span>응답 점수</span>
                <strong>{detail.expert.responseScore ?? '-'}</strong>
                <span>리뷰 점수</span>
                <strong>{detail.expert.reviewScore ?? '-'}</strong>
              </div>
              <div className="detail-block">
                <strong>서비스 지역</strong>
                <ul>
                  {detail.expert.serviceRegions.map((region) => (
                    <li key={`${region.sido}-${region.sigungu ?? 'all'}`}>
                      {region.sido} {region.sigungu ?? ''}
                    </li>
                  ))}
                </ul>
              </div>
              <div className="detail-block">
                <strong>포트폴리오</strong>
                <ul>
                  {detail.expert.portfolios.map((portfolio) => (
                    <li key={portfolio.id}>
                      {portfolio.title}
                      {portfolio.description
                        ? ` · ${portfolio.description}`
                        : ''}
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          ) : (
            <p className="empty">
              왼쪽 목록에서 추천 전문가를 선택하면 상세가 표시됩니다.
            </p>
          )}
        </article>
      </section>

      <section className="panel">
        <div className="section-head">
          <p className="eyebrow">문의 등록</p>
          <h2>{selectedExpert?.companyName ?? '선택된 전문가 없음'}</h2>
        </div>
        <div className="summary-card">
          <label className="field">
            <span>요청 카테고리</span>
            <select
              onChange={(event) => setRequestedCategoryKey(event.target.value)}
              value={requestedCategoryKey}
            >
              <option value="">선택하세요</option>
              {Object.entries(categoryNameMap).map(([key, name]) => (
                <option key={key} value={key}>
                  {name}
                </option>
              ))}
            </select>
          </label>
          <div className="grid two-up">
            <label className="field">
              <span>예산 최소</span>
              <input
                onChange={(event) => setBudgetMin(event.target.value)}
                value={budgetMin}
              />
            </label>
            <label className="field">
              <span>예산 최대</span>
              <input
                onChange={(event) => setBudgetMax(event.target.value)}
                value={budgetMax}
              />
            </label>
          </div>
          <label className="field">
            <span>희망 시작일</span>
            <input
              onChange={(event) => setDesiredStartDate(event.target.value)}
              type="date"
              value={desiredStartDate}
            />
          </label>
          <label className="field">
            <span>문의 내용</span>
            <textarea
              className="multiline-input"
              onChange={(event) => setMessage(event.target.value)}
              rows={5}
              value={message}
            />
          </label>
          <button className="primary-button" onClick={() => void submitLead()}>
            전문가 문의 등록
          </button>
        </div>
      </section>
    </main>
  );
}
