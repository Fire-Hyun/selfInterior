# ProcessPlan 카테고리 설계

## 범위

이 문서는 `ProcessPlan` vertical slice에서 구현하는 공정 카탈로그, 프로젝트별 공정 플랜, 단계 상세, 체크리스트 토글 구조를 정의한다.

## 사용자 흐름

1. 사용자는 프로젝트 홈에서 공정 플래너로 이동한다.
2. 시스템은 선택된 도면 후보와 프로젝트 범위를 기준으로 공정 플랜 생성 가능 여부를 판단한다.
3. 사용자는 공정 플랜을 생성한다.
4. 시스템은 카탈로그 seed를 기반으로 프로젝트별 단계와 체크리스트 snapshot을 만든다.
5. 사용자는 단계 목록을 확인하고 현재 단계 상세를 연다.
6. 사용자는 체크리스트를 완료 처리한다.
7. 시스템은 단계 완료 여부와 `projects.current_process_step`를 갱신한다.

## 도메인 모델 요약

### `ProcessCatalog`

- `step_key`
- `title`
- `description`
- `sort_order`
- `default_duration_days`
- 적용 프로젝트 유형 목록
- 적용 거주 상태 목록

### `ProcessGuide`

- 목적
- 시작 전 체크리스트 설명
- 결정 포인트
- 셀프 가능 범위
- 전문가 필수 범위
- 자주 하는 실수
- 다음 단계 전 확인 사항

### `ProjectProcessPlan`

- `project_id`
- `plan_status`
- 생성 기준 도면 후보
- 현재 단계
- 단계 목록

### `ProjectProcessStep`

- `step_key`
- `title`
- `status`
- `sort_order`
- `duration_days`
- `is_required`

### `ProjectProcessTask`

- `task_group`
- `title`
- `description`
- `completed`
- `completed_at`

## 애플리케이션 계층

### API

- controller: 플랜 생성/조회, 단계 상세, 체크리스트 토글
- application service: 카탈로그 snapshot 생성, 현재 단계 계산
- persistence: 공정 카탈로그 seed, 프로젝트별 플랜/단계/태스크 저장

### Web

- 공정 플랜 생성 버튼
- 단계 타임라인
- 단계 상세 카드
- 체크리스트 토글

## 생성 규칙

- `FULL` 프로젝트는 전체 공정 흐름을 생성한다.
- `PARTIAL` 프로젝트는 철거/습식 등 고강도 공정 일부를 제외한 경량 흐름을 우선 생성한다.
- `ISSUE_CHECK` 프로젝트는 진단과 복구 중심 최소 흐름을 생성한다.
- `OCCUPIED` 상태는 준비 단계 설명과 체크리스트를 보수적으로 생성한다.
- 선택된 도면 후보가 없으면 플랜 생성은 차단한다.

## persistence 최소 테이블

- `process_catalogs`
- `process_guides`
- `process_checklists`
- `project_process_plans`
- `project_process_steps`
- `project_process_tasks`

## 테스트 포인트

- 공정 카탈로그 seed가 존재한다.
- 프로젝트 유형별로 다른 단계 구성이 생성된다.
- 선택된 도면 후보가 없으면 공정 플랜 생성이 실패한다.
- 체크리스트 토글 시 단계 상태가 갱신된다.
- 프로젝트 홈이 공정 플랜 상태를 반영한다.
