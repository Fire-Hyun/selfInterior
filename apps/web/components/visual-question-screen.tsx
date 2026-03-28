'use client';

import type {
  CreateVisualQuestionResponse,
  ProcessPlanResponse,
  SpaceType,
  VisualQuestionDetailResponse,
  VisualQuestionListResponse,
  VisualQuestionSummary,
} from '@selfinterior/shared-types';
import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { ApiRequestError, apiRequest } from '@/lib/api';

const SPACE_OPTIONS: SpaceType[] = [
  'LIVING_ROOM',
  'KITCHEN',
  'BATHROOM',
  'BEDROOM',
  'ENTRANCE',
  'BALCONY',
  'OTHER',
];

export function VisualQuestionScreen({ projectId }: { projectId: string }) {
  const [busy, setBusy] = useState('');
  const [error, setError] = useState('');
  const [questionText, setQuestionText] = useState(
    '이 부분 상태가 괜찮은지 궁금해요.',
  );
  const [processStepKey, setProcessStepKey] = useState('');
  const [spaceType, setSpaceType] = useState<SpaceType>('BATHROOM');
  const [files, setFiles] = useState<File[]>([]);
  const [questions, setQuestions] = useState<VisualQuestionSummary[]>([]);
  const [selectedQuestionId, setSelectedQuestionId] = useState('');
  const [detail, setDetail] = useState<VisualQuestionDetailResponse | null>(
    null,
  );

  const loadQuestions = useCallback(async () => {
    try {
      const response = await apiRequest<VisualQuestionListResponse>(
        `/api/v1/projects/${projectId}/visual-questions`,
      );
      const nextQuestions = response.data?.questions ?? [];
      setQuestions(nextQuestions);
      if (!selectedQuestionId && nextQuestions[0]) {
        setSelectedQuestionId(nextQuestions[0].id);
      }
    } catch (caughtError) {
      if (
        caughtError instanceof ApiRequestError &&
        caughtError.status === 404
      ) {
        setQuestions([]);
        return;
      }
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '질문 목록을 불러오지 못했습니다.',
      );
    }
  }, [projectId, selectedQuestionId]);

  const loadProcessPlanHint = useCallback(async () => {
    try {
      const response = await apiRequest<ProcessPlanResponse>(
        `/api/v1/projects/${projectId}/process-plan`,
      );
      if (response.data?.currentStepKey) {
        setProcessStepKey(response.data.currentStepKey);
      }
    } catch {
      // 공정 플랜이 없어도 질문 등록은 가능하다.
    }
  }, [projectId]);

  const loadDetail = useCallback(
    async (questionId: string) => {
      try {
        const response = await apiRequest<VisualQuestionDetailResponse>(
          `/api/v1/projects/${projectId}/visual-questions/${questionId}`,
        );
        setDetail(response.data ?? null);
      } catch (caughtError) {
        setError(
          caughtError instanceof Error
            ? caughtError.message
            : '질문 상세를 불러오지 못했습니다.',
        );
      }
    },
    [projectId],
  );

  useEffect(() => {
    void loadQuestions();
    void loadProcessPlanHint();
  }, [loadProcessPlanHint, loadQuestions]);

  useEffect(() => {
    if (!selectedQuestionId) {
      setDetail(null);
      return;
    }
    void loadDetail(selectedQuestionId);
  }, [loadDetail, selectedQuestionId]);

  async function submitQuestion() {
    if (files.length === 0) {
      setError('최소 1장의 사진을 선택하세요.');
      return;
    }

    setBusy('사진 질문을 등록하는 중입니다.');
    setError('');

    try {
      const payload = new FormData();
      payload.append('questionText', questionText);
      payload.append('spaceType', spaceType);
      if (processStepKey) {
        payload.append('processStepKey', processStepKey);
      }
      for (const file of files) {
        payload.append('files', file);
      }

      const response = await apiRequest<CreateVisualQuestionResponse>(
        `/api/v1/projects/${projectId}/visual-questions`,
        {
          method: 'POST',
          body: payload,
        },
      );

      const createdQuestionId = response.data?.questionId ?? '';
      setFiles([]);
      await loadQuestions();
      if (createdQuestionId) {
        setSelectedQuestionId(createdQuestionId);
        await loadDetail(createdQuestionId);
      }
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '사진 질문 등록에 실패했습니다.',
      );
    } finally {
      setBusy('');
    }
  }

  return (
    <main className="shell">
      <section className="hero-card">
        <div className="hero-copy">
          <p className="eyebrow">Visual Question</p>
          <h1>
            현장 사진을 기준으로 위험도와 다음 확인 사항을 정리하는 질문 흐름
          </h1>
          <p className="lede">
            사진과 질문을 등록하면 구조화된 답변을 저장하고, 프로젝트 홈 최근
            질문 카드와 연결합니다.
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
              href={`/projects/${projectId}/process`}
            >
              공정 플래너 보기
            </Link>
          </div>
        </div>
        <div className="hero-panel">
          {busy ? <p className="status">{busy}</p> : null}
          {error ? <p className="error">{error}</p> : null}
          <div className="summary-grid">
            <span>현재 공정</span>
            <strong>{processStepKey || '미지정'}</strong>
            <span>등록 질문 수</span>
            <strong>{questions.length}</strong>
            <span>기본 공간</span>
            <strong>{spaceType}</strong>
          </div>
        </div>
      </section>

      <section className="grid two-up">
        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">질문 등록</p>
            <h2>사진과 질문을 함께 올리기</h2>
          </div>
          <div className="summary-card">
            <label className="field">
              <span>질문 내용</span>
              <textarea
                className="multiline-input"
                onChange={(event) => setQuestionText(event.target.value)}
                rows={4}
                value={questionText}
              />
            </label>
            <label className="field">
              <span>공정 단계</span>
              <input
                onChange={(event) => setProcessStepKey(event.target.value)}
                placeholder="예: ELECTRICAL"
                value={processStepKey}
              />
            </label>
            <label className="field">
              <span>공간</span>
              <select
                onChange={(event) =>
                  setSpaceType(event.target.value as SpaceType)
                }
                value={spaceType}
              >
                {SPACE_OPTIONS.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              <span>사진 업로드</span>
              <input
                multiple
                onChange={(event) =>
                  setFiles(Array.from(event.target.files ?? []))
                }
                type="file"
              />
            </label>
            <button
              className="primary-button"
              onClick={() => void submitQuestion()}
            >
              사진 질문 등록
            </button>
          </div>
        </article>

        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">질문 목록</p>
            <h2>최근 등록 질문</h2>
          </div>
          <div className="task-list">
            {questions.length === 0 ? (
              <p className="empty">아직 등록된 질문이 없습니다.</p>
            ) : (
              questions.map((question) => (
                <button
                  className={`task-card tone-ready ${
                    selectedQuestionId === question.id ? 'selected-outline' : ''
                  }`}
                  key={question.id}
                  onClick={() => setSelectedQuestionId(question.id)}
                >
                  <div className="plan-topline">
                    <strong>{question.spaceType}</strong>
                    <span className="badge">
                      {question.riskLevel ?? question.status}
                    </span>
                  </div>
                  <p>{question.questionText}</p>
                  <small>{question.observedText ?? '답변 준비 중'}</small>
                </button>
              ))
            )}
          </div>
        </article>
      </section>

      <section className="panel">
        <div className="section-head">
          <p className="eyebrow">답변 상세</p>
          <h2>{detail?.question.questionText ?? '질문을 선택하세요'}</h2>
        </div>
        {detail ? (
          <div className="grid two-up">
            <div className="summary-card">
              <div className="plan-topline">
                <strong>{detail.question.spaceType}</strong>
                <span className="badge">{detail.answer.riskLevel}</span>
              </div>
              <div className="stack">
                <div className="detail-block">
                  <strong>관찰 내용</strong>
                  <p>{detail.answer.observedText}</p>
                </div>
                <div className="detail-block">
                  <strong>가능한 원인</strong>
                  <p>{detail.answer.possibleCausesText}</p>
                </div>
                <div className="detail-block">
                  <strong>추가 확인</strong>
                  <p>{detail.answer.nextChecksText}</p>
                </div>
                <div className="detail-block">
                  <strong>진행 권고</strong>
                  <p>{detail.answer.proceedRecommendationText}</p>
                </div>
              </div>
            </div>

            <div className="summary-card">
              <div className="summary-grid">
                <span>전문가 필요</span>
                <strong>
                  {detail.answer.expertRequired ? 'true' : 'false'}
                </strong>
                <span>신뢰도</span>
                <strong>{detail.answer.confidenceScore.toFixed(1)}</strong>
                <span>공정 단계</span>
                <strong>{detail.question.processStepKey ?? '미지정'}</strong>
              </div>
              <div className="detail-block">
                <strong>업로드 이미지</strong>
                <ul>
                  {detail.images.map((image) => (
                    <li key={image.id}>
                      {image.fileName} · {image.storagePath}
                    </li>
                  ))}
                </ul>
              </div>
              <div className="detail-block">
                <strong>관련 링크</strong>
                <div className="stack">
                  {detail.relatedGuideLinks.map((link) => (
                    <Link
                      className="link-button"
                      href={link.path}
                      key={link.slug}
                    >
                      {link.title}
                    </Link>
                  ))}
                  {detail.answer.expertRequired ? (
                    <Link
                      className="link-button"
                      href={`/projects/${projectId}/experts`}
                    >
                      전문가 추천과 문의 보기
                    </Link>
                  ) : null}
                </div>
              </div>
            </div>
          </div>
        ) : (
          <p className="empty">질문을 선택하면 상세 답변이 여기 표시됩니다.</p>
        )}
      </section>
    </main>
  );
}
