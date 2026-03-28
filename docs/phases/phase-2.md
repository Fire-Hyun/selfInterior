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

## 이번 작업 단위 상세

### 설계

- `ProcessPlan`은 공통 카탈로그를 기반으로 프로젝트별 snapshot을 생성한다.
- seed 데이터는 `process_catalogs`, `process_guides`, `process_checklists`에 저장한다.
- 프로젝트별 상태는 `project_process_plans`, `project_process_steps`, `project_process_tasks`에 저장한다.
- 플랜 생성은 선택된 도면 후보가 있는 프로젝트만 허용한다.
- 생성 이후 `projects.current_process_step`를 첫 단계로 맞추고, 체크리스트 완료 상태에 따라 현재 단계를 갱신할 수 있게 한다.

### API

- `POST /api/v1/projects/{projectId}/process-plan/generate`
- `GET /api/v1/projects/{projectId}/process-plan`
- `GET /api/v1/projects/{projectId}/process-plan/steps/{stepKey}`
- `PATCH /api/v1/projects/{projectId}/process-plan/tasks/{taskId}`

### Web

- `app/projects/[projectId]/process` route 추가
- 공정 플랜 생성 버튼, 단계 타임라인, 단계 상세 패널, 체크리스트 토글 UI 제공
- 프로젝트 홈에서 공정 플래너 화면으로 이동 가능하게 연결

## 완료 기준

- 설계 문서가 `ProcessPlan` 구조를 반영한다.
- 프로젝트별 공정 플랜을 생성하고 다시 조회할 수 있다.
- 단계 상세와 체크리스트 완료 상태를 web에서 확인하고 수정할 수 있다.
- 프로젝트 홈이 실제 공정 플랜 상태를 참조한다.
- `format`, `lint`, `typecheck`, `test`, `docs:check`, `build`가 green이다.
- 커밋 및 push 시도 결과가 기록된다.
