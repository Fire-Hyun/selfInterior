'use client';

import type {
  GenerateStyleImagesResponse,
  SpaceType,
  StyleImage,
  StyleImageListResponse,
  StylePreset,
  StylePresetListResponse,
} from '@selfinterior/shared-types';
import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { apiRequest } from '@/lib/api';

const SPACE_OPTIONS: SpaceType[] = [
  'LIVING_ROOM',
  'KITCHEN',
  'BATHROOM',
  'BEDROOM',
  'ENTRANCE',
];

const BUDGET_LEVELS = ['LOW', 'MID', 'HIGH'] as const;

export function StyleGenerationScreen({ projectId }: { projectId: string }) {
  const [busy, setBusy] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [presets, setPresets] = useState<StylePreset[]>([]);
  const [images, setImages] = useState<StyleImage[]>([]);
  const [selectedPresetKey, setSelectedPresetKey] = useState('WHITE_MINIMAL');
  const [budgetLevel, setBudgetLevel] =
    useState<(typeof BUDGET_LEVELS)[number]>('MID');
  const [extraPrompt, setExtraPrompt] = useState(
    '실거주 기준으로 무난하고 관리가 쉬운 분위기',
  );
  const [keepItemsText, setKeepItemsText] = useState(
    'WINDOWS, KITCHEN_CABINET',
  );
  const [selectedSpaces, setSelectedSpaces] = useState<SpaceType[]>([
    'LIVING_ROOM',
    'KITCHEN',
  ]);

  const load = useCallback(async () => {
    const [presetResponse, imageResponse] = await Promise.all([
      apiRequest<StylePresetListResponse>('/api/v1/style-presets'),
      apiRequest<StyleImageListResponse>(
        `/api/v1/projects/${projectId}/styles/images`,
      ),
    ]);

    const nextPresets = presetResponse.data?.presets ?? [];
    setPresets(nextPresets);
    if (!nextPresets.some((preset) => preset.key === selectedPresetKey)) {
      setSelectedPresetKey(nextPresets[0]?.key ?? '');
    }
    setImages(imageResponse.data?.images ?? []);
  }, [projectId, selectedPresetKey]);

  useEffect(() => {
    async function start() {
      setError('');
      try {
        await load();
      } catch (caughtError) {
        setError(
          caughtError instanceof Error
            ? caughtError.message
            : '스타일 데이터를 불러오지 못했습니다.',
        );
      }
    }

    void start();
  }, [load]);

  function toggleSpace(spaceType: SpaceType) {
    setSelectedSpaces((current) =>
      current.includes(spaceType)
        ? current.filter((space) => space !== spaceType)
        : [...current, spaceType],
    );
  }

  async function generateImages() {
    if (!selectedPresetKey || selectedSpaces.length === 0) {
      setError('스타일 preset과 공간을 먼저 선택하세요.');
      return;
    }

    setBusy('스타일 이미지를 생성하는 중입니다.');
    setError('');
    setSuccess('');

    try {
      const keepItems = keepItemsText
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean);

      const response = await apiRequest<GenerateStyleImagesResponse>(
        `/api/v1/projects/${projectId}/styles/generate`,
        {
          method: 'POST',
          body: JSON.stringify({
            spaceTypes: selectedSpaces,
            stylePresetKey: selectedPresetKey,
            budgetLevel,
            keepItems,
            extraPrompt,
          }),
        },
      );
      setSuccess(
        `${response.data?.imageCount ?? 0}장의 스타일 카드가 생성되었습니다.`,
      );
      await load();
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '스타일 이미지 생성에 실패했습니다.',
      );
    } finally {
      setBusy('');
    }
  }

  async function likeImage(imageId: string) {
    setBusy('선택 상태를 저장하는 중입니다.');
    setError('');
    setSuccess('');

    try {
      await apiRequest(
        `/api/v1/projects/${projectId}/styles/images/${imageId}/like`,
        {
          method: 'POST',
        },
      );
      setSuccess('좋아요와 스타일 선택 상태를 저장했습니다.');
      await load();
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : '좋아요 저장에 실패했습니다.',
      );
    } finally {
      setBusy('');
    }
  }

  const selectedPreset = presets.find(
    (preset) => preset.key === selectedPresetKey,
  );

  return (
    <main className="shell">
      <section className="hero-card">
        <div className="hero-copy">
          <p className="eyebrow">Style</p>
          <h1>프로젝트 문맥을 반영한 스타일 preset과 mock 이미지 생성</h1>
          <p className="lede">
            공간, 유지 요소, 예산 수준을 반영해 스타일 카드를 만들고 좋아요한
            이미지를 프로젝트 선택 상태로 저장합니다.
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
            <Link
              className="link-button inline-link"
              href={`/projects/${projectId}/experts`}
            >
              전문가 추천 보기
            </Link>
          </div>
        </div>
        <div className="hero-panel">
          {busy ? <p className="status">{busy}</p> : null}
          {error ? <p className="error">{error}</p> : null}
          {success ? <p className="status">{success}</p> : null}
          <div className="summary-grid">
            <span>preset</span>
            <strong>{selectedPreset?.name ?? '미선택'}</strong>
            <span>선택 공간 수</span>
            <strong>{selectedSpaces.length}</strong>
            <span>생성 카드 수</span>
            <strong>{images.length}</strong>
          </div>
        </div>
      </section>

      <section className="grid two-up">
        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">생성 조건</p>
            <h2>preset과 공간 선택</h2>
          </div>
          <div className="summary-card">
            <label className="field">
              <span>스타일 preset</span>
              <select
                onChange={(event) => setSelectedPresetKey(event.target.value)}
                value={selectedPresetKey}
              >
                {presets.map((preset) => (
                  <option key={preset.id} value={preset.key}>
                    {preset.name}
                  </option>
                ))}
              </select>
            </label>
            <div className="detail-block">
              <strong>공간 선택</strong>
              <div className="stack">
                {SPACE_OPTIONS.map((spaceType) => (
                  <label className="field-checkbox" key={spaceType}>
                    <input
                      checked={selectedSpaces.includes(spaceType)}
                      onChange={() => toggleSpace(spaceType)}
                      type="checkbox"
                    />
                    <span>{spaceType}</span>
                  </label>
                ))}
              </div>
            </div>
            <label className="field">
              <span>예산 수준</span>
              <select
                onChange={(event) =>
                  setBudgetLevel(
                    event.target.value as (typeof BUDGET_LEVELS)[number],
                  )
                }
                value={budgetLevel}
              >
                {BUDGET_LEVELS.map((level) => (
                  <option key={level} value={level}>
                    {level}
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              <span>유지 요소</span>
              <input
                onChange={(event) => setKeepItemsText(event.target.value)}
                value={keepItemsText}
              />
            </label>
            <label className="field">
              <span>추가 프롬프트</span>
              <textarea
                className="multiline-input"
                onChange={(event) => setExtraPrompt(event.target.value)}
                rows={4}
                value={extraPrompt}
              />
            </label>
            <button
              className="primary-button"
              onClick={() => void generateImages()}
            >
              스타일 카드 생성
            </button>
          </div>
        </article>

        <article className="panel">
          <div className="section-head">
            <p className="eyebrow">preset 설명</p>
            <h2>{selectedPreset?.name ?? 'preset 미선택'}</h2>
          </div>
          {selectedPreset ? (
            <div className="summary-card">
              <p>{selectedPreset.description}</p>
              <div className="detail-block">
                <strong>prompt template</strong>
                <p>{selectedPreset.promptTemplate}</p>
              </div>
            </div>
          ) : (
            <p className="empty">사용 가능한 preset을 불러오는 중입니다.</p>
          )}
        </article>
      </section>

      <section className="panel">
        <div className="section-head">
          <p className="eyebrow">생성 결과</p>
          <h2>스타일 카드 그리드</h2>
        </div>
        {images.length === 0 ? (
          <p className="empty">아직 생성된 스타일 카드가 없습니다.</p>
        ) : (
          <div className="task-list">
            {images.map((image) => (
              <article className="task-card tone-ready" key={image.id}>
                <div className="plan-topline">
                  <strong>
                    {image.stylePresetName} · {image.spaceType}
                  </strong>
                  <span className="badge">
                    {image.liked ? 'SELECTED' : image.generationStatus}
                  </span>
                </div>
                <p>{image.promptText}</p>
                <div className="summary-grid">
                  <span>난이도</span>
                  <strong>{image.difficulty}</strong>
                  <span>예산 영향</span>
                  <strong>{image.budgetImpact}</strong>
                  <span>모델</span>
                  <strong>{image.modelName ?? '-'}</strong>
                </div>
                <div className="detail-block">
                  <strong>필요 공정</strong>
                  <p>{image.suggestedProcessSteps.join(', ')}</p>
                </div>
                <div className="detail-block">
                  <strong>mock storage</strong>
                  <p>{image.storageKey}</p>
                </div>
                <button
                  className="primary-button"
                  onClick={() => void likeImage(image.id)}
                >
                  이 스타일로 계획 만들기
                </button>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
