# Phase 1 구현 계획

## 목적

주소-first 온보딩의 첫 실행 단위를 실제 사용 흐름으로 닫는다.

## Phase 1 범위

### Phase 1.0

- monorepo 부트스트랩
- 주소 검색
- 집 정보 resolve
- 프로젝트 생성
- 프로젝트-속성 연결
- 도면 후보 저장/조회

### Phase 1.1

- 프로젝트 상세 응답에 집 요약과 선택된 도면 요약 포함
- 도면 후보 선택 API 추가
- web에서 생성된 프로젝트의 현재 상태를 바로 확인할 수 있는 상세 패널 추가
- 후보 선택 후 프로젝트 상세와 도면 목록이 함께 갱신되는 흐름 구성

### Phase 1.2

- `/projects/{projectId}/home`에 대응하는 프로젝트 홈 read model 추가
- 우리 집 요약, 지금 해야 할 일, 최근 질문, 추천 전문가 카드를 한 번에 반환하는 API 구성
- `ProcessPlan`, `VisualQuestion`, `ExpertLead`는 placeholder 카드로 먼저 노출하고 이후 phase에서 실제 도메인으로 치환
- web에서 프로젝트 홈 화면을 별도 route로 제공하고 온보딩 화면과 연결

## 이번 작업 단위 상세

### 설계

- `GET /api/v1/projects/{projectId}/home`은 아래를 함께 반환한다.
  - 프로젝트 기본 정보
  - `Property` 기반 우리 집 요약 카드
  - 선택된 `FloorPlanCandidate` 기반 도면 요약 카드
  - 다음 액션 리스트
  - `VisualQuestion`, `ExpertLead` placeholder 카드
- 프로젝트 홈은 기존 엔티티를 조합하는 read model로 구현하고, 아직 없는 도메인은 상태 카드로만 노출한다.

### API

- `GET /api/v1/projects/{projectId}/home`
- 응답은 프로젝트 홈 화면이 필요한 카드를 한 번에 반환한다.
- 다음 액션은 `Property`, `FloorPlanCandidate`, `NormalizedPlan` 상태로부터 계산한다.
- 최근 질문/추천 전문가는 placeholder 상태와 다음 연결 예정 이유를 함께 반환한다.

### Web

- `app/projects/[projectId]/home` route 추가
- 프로젝트 생성 후 홈 route로 이동할 수 있는 CTA 추가
- 프로젝트 홈 화면에 카드형 요약, 액션, placeholder 영역 표시
- 기존 홈 화면의 프로젝트 목록에서도 각 프로젝트 홈으로 진입 가능하게 연결

## 완료 기준

- 설계 문서가 구현 구조를 반영한다.
- 프로젝트 홈 응답으로 집/도면/다음 액션/placeholder 상태를 한 번에 볼 수 있다.
- 사용자가 온보딩 후 프로젝트 홈으로 이동할 수 있다.
- `ProcessPlan`, `VisualQuestion`, `ExpertLead`의 향후 연결 위치가 홈 카드에서 명확히 드러난다.
- `format`, `lint`, `typecheck`, `test`, `docs:check`, `build`가 green이다.
- 커밋 및 push 시도 결과가 기록된다.
