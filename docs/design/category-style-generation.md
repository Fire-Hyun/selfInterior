# StyleGeneration 카테고리 설계

## 범위

이 문서는 `StylePreset`, `ProjectStyleSelection`, `GeneratedStyleImage` vertical slice에서 구현하는 스타일 preset 조회, 스타일 이미지 생성, 좋아요/선택 저장을 정의한다.

## 사용자 흐름

1. 사용자는 프로젝트 스타일 페이지에서 공간과 스타일 preset을 선택한다.
2. 시스템은 프로젝트 범위, 유지 요소, 추가 프롬프트를 조합해 mock style image provider를 호출한다.
3. 생성된 이미지 카드는 난이도, 예산 영향, 필요한 공정 힌트와 함께 저장된다.
4. 사용자는 마음에 드는 이미지를 좋아요하고 프로젝트 스타일 선택으로 고정한다.

## 도메인 모델 요약

### `StylePreset`

- `key`
- `name`
- `description`
- `prompt_template`
- `active`

### `ProjectStyleSelection`

- `project_id`
- `style_preset_id`
- `space_type`
- `priority`
- `selected`

### `GeneratedStyleImage`

- `project_id`
- `style_preset_id`
- `space_type`
- `prompt_text`
- `negative_prompt_text`
- `generation_status`
- `storage_key`
- `thumbnail_key`
- `seed`
- `model_name`
- `metadata`
- `liked`

## 생성 규칙

- 스타일 생성은 프로젝트 단위로 수행한다.
- `keepItems`와 `extraPrompt`는 prompt text에 직접 반영한다.
- mock provider는 공간별 1장 이상 이미지를 생성한다.
- 이미지 metadata에는 `difficulty`, `budgetImpact`, `suggestedProcessSteps`를 포함한다.
- 좋아요 시 같은 `space_type` 기준 기존 선택을 해제하고 새 선택을 `selected=true`로 저장한다.

## 애플리케이션 계층

### API

- controller: preset 조회, 생성, 목록, 좋아요
- application service: prompt 조합, provider 호출, 선택 상태 갱신
- adapter: `StyleImageProvider`, `MockStyleImageProvider`

### Web

- 스타일 preset 선택 UI
- 공간 선택 UI
- 이미지 그리드
- 좋아요/선택 CTA

## persistence 최소 테이블

- `style_presets`
- `project_style_selections`
- `generated_style_images`

## 테스트 포인트

- preset seed가 active 상태로 조회된다.
- 스타일 이미지 생성 시 prompt/model/metadata가 저장된다.
- 이미지 목록이 프로젝트별 최신순으로 조회된다.
- 좋아요 시 해당 이미지가 선택 상태로 반영된다.
