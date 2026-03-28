# Phase 3 구현 계획

## 목적

프로젝트 기준 구조와 공정 문맥이 정리된 이후, 집 상황에 맞는 스타일 preset과 mock 스타일 이미지를 생성해 후속 의사결정 흐름을 닫는다.

## Phase 3 범위

### Phase 3.0

- 스타일 preset seed 추가
- mock style image provider 추가
- 프로젝트별 스타일 이미지 생성/조회 API 추가
- 스타일 이미지 좋아요와 선택 상태 저장
- web에 스타일 페이지 route 추가

## 이번 작업 단위 상세

### 설계

- `style_presets`는 운영 가능한 기본 스타일 카탈로그로 seed한다.
- 스타일 생성은 `StyleImageProvider` adapter 뒤에 두고 이번 단계에서는 mock provider를 사용한다.
- 생성 요청에는 `spaceTypes`, `stylePresetKey`, `budgetLevel`, `keepItems`, `extraPrompt`를 반영한다.
- 결과에는 `prompt_text`, `model_name`, `metadata`, `liked`를 저장한다.
- `project_style_selections`는 프로젝트별 선택 스타일 상태를 유지한다.

### API

- `GET /api/v1/style-presets`
- `POST /api/v1/projects/{projectId}/styles/generate`
- `GET /api/v1/projects/{projectId}/styles/images`
- `POST /api/v1/projects/{projectId}/styles/images/{imageId}/like`

### Web

- `app/projects/[projectId]/style` route 추가
- 스타일 preset 선택, 공간 선택, 추가 프롬프트 입력 UI 제공
- 생성된 이미지 그리드와 좋아요 CTA 제공
- “이 스타일로 계획 만들기” 액션을 좋아요/선택 상태로 연결

## 완료 기준

- 설계 문서가 `StylePreset`과 `GeneratedStyleImage` 구조를 반영한다.
- 프로젝트에서 스타일 이미지를 생성하고 다시 조회할 수 있다.
- mock provider 결과가 prompt/model/metadata와 함께 저장된다.
- 사용자가 좋아요를 눌러 선택 상태를 저장할 수 있다.
- `format`, `lint`, `typecheck`, `test`, `docs:check`, `build`가 green이다.
- 커밋 및 push 시도 결과가 기록된다.
