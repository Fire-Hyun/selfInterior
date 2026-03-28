# Phase 2 구현 계획

## 목적

도면 기준이 정해진 프로젝트에 대해 실제 실행 가능한 `ProcessPlan` 초안을 생성하고, 프로젝트 홈의 다음 액션을 공정 플래너와 연결한다.

## Phase 2 범위

### Phase 2.0

- 공정 카탈로그 seed 추가
- 프로젝트별 공정 플랜 생성 API 추가
- 공정 단계 상세 조회와 체크리스트 토글 API 추가
- web에 공정 플래너 route 추가
- 프로젝트 홈의 process placeholder를 실제 공정 플랜 액션으로 치환

### Phase 2.1

- 사진 질문 등록/조회 API 추가
- 이미지 저장 mock adapter와 Vision QA mock adapter 추가
- 구조화된 답변과 risk level 저장
- web에 사진 질문 route 추가
- 프로젝트 홈의 최근 질문 placeholder를 실제 최근 질문 카드로 치환

## 이번 작업 단위 상세

### Phase 2.0 설계

- `ProcessPlan`은 공통 카탈로그를 기반으로 프로젝트별 snapshot을 생성한다.
- seed 데이터는 `process_catalogs`, `process_guides`, `process_checklists`에 저장한다.
- 프로젝트별 상태는 `project_process_plans`, `project_process_steps`, `project_process_tasks`에 저장한다.
- 플랜 생성은 선택된 도면 후보가 있는 프로젝트만 허용한다.
- 생성 이후 `projects.current_process_step`를 첫 단계로 맞추고, 체크리스트 완료 상태에 따라 현재 단계를 갱신할 수 있게 한다.

### Phase 2.0 API

- `POST /api/v1/projects/{projectId}/process-plan/generate`
- `GET /api/v1/projects/{projectId}/process-plan`
- `GET /api/v1/projects/{projectId}/process-plan/steps/{stepKey}`
- `PATCH /api/v1/projects/{projectId}/process-plan/tasks/{taskId}`

### Phase 2.0 Web

- `app/projects/[projectId]/process` route 추가
- 공정 플랜 생성 버튼, 단계 타임라인, 단계 상세 패널, 체크리스트 토글 UI 제공
- 프로젝트 홈에서 공정 플래너 화면으로 이동 가능하게 연결

### Phase 2.1 설계

- `VisualQuestion`은 프로젝트 단위 사진 질문과 구조화된 답변을 저장한다.
- 이미지 저장은 `VisualQuestionStorage` adapter 뒤에 두고, 이번 단계에서는 로컬 mock storage를 사용한다.
- 분석은 `VisionQaClient` adapter 뒤에 두고, 이번 단계에서는 동기 mock 분석으로 답변을 생성한다.
- 답변에는 `risk_level`, `confidence_score`, `expert_required`, 추가 확인 및 진행 권고를 저장한다.
- 프로젝트 홈은 가장 최근 질문과 답변 위험도를 카드로 노출한다.

### Phase 2.1 API

- `POST /api/v1/projects/{projectId}/visual-questions`
- `GET /api/v1/projects/{projectId}/visual-questions`
- `GET /api/v1/projects/{projectId}/visual-questions/{questionId}`

### Phase 2.1 Web

- `app/projects/[projectId]/qa` route 추가
- 사진 업로드, 질문 내용, 공간 유형, 공정 단계 입력 UI 제공
- 최신 질문 목록과 답변 상세, 관련 공정 링크를 함께 노출

## 완료 기준

- 설계 문서가 `ProcessPlan` 구조를 반영한다.
- 프로젝트별 공정 플랜을 생성하고 다시 조회할 수 있다.
- 단계 상세와 체크리스트 완료 상태를 web에서 확인하고 수정할 수 있다.
- 프로젝트 홈이 실제 공정 플랜 상태를 참조한다.
- 설계 문서가 `VisualQuestion` 구조를 반영한다.
- 사진 질문을 등록하고 질문 목록 및 상세 답변을 다시 조회할 수 있다.
- 프로젝트 홈이 최근 질문을 placeholder 대신 실제 카드로 표시한다.
- `format`, `lint`, `typecheck`, `test`, `docs:check`, `build`가 green이다.
- 커밋 및 push 시도 결과가 기록된다.
