# ADR-0002 프로젝트 홈 read model을 별도 구성한다

## 상태

승인

## 배경

설계서 기준으로 `/projects/{projectId}/home`은 사용자가 다음 행동을 한 번에 이해하는 프로젝트 홈 역할을 가져야 한다.
하지만 현재 구현 범위는 `Project`, `Property`, `FloorPlanCandidate`, `NormalizedPlan`까지만 실제 persistence가 준비되어 있고, `ProcessPlan`, `VisualQuestion`, `ExpertLead`는 아직 후속 phase 대상이다.

이 상태에서 프로젝트 홈을 만들려면 두 가지 선택지가 있다.

1. 기존 `GET /api/v1/projects/{projectId}`를 계속 확장한다.
2. 프로젝트 홈 전용 read model을 별도 구성한다.

## 결정

프로젝트 홈은 `GET /api/v1/projects/{projectId}/home` 전용 read model로 분리한다.

구성 원칙:

- `Project`, `Property`, `FloorPlanCandidate`, `NormalizedPlan`에서 실제 데이터를 읽어 카드형 응답으로 조합한다.
- `ProcessPlan`, `VisualQuestion`, `ExpertLead`는 아직 persistence를 만들지 않고 placeholder 카드로 먼저 노출한다.
- placeholder 카드에는 `status`, `description`, `primaryActionPath`를 넣어 이후 실제 도메인으로 치환 가능한 UI 계약을 유지한다.
- 기존 `GET /api/v1/projects/{projectId}`는 상세 조회 용도로 유지하고, 프로젝트 홈은 사용자 행동 중심 응답으로 분리한다.

## 결과

장점:

- 프로젝트 홈 UI가 필요한 데이터를 한 번에 받을 수 있다.
- 아직 구현되지 않은 도메인을 무리하게 persistence로 앞당기지 않아도 된다.
- 후속 phase에서 `ProcessPlan`, `VisualQuestion`, `ExpertLead`를 실제 테이블/서비스로 연결할 때 응답 계약을 단계적으로 유지할 수 있다.

단점:

- 프로젝트 상세와 프로젝트 홈 사이에 일부 중복 read model이 생긴다.
- 홈 카드 계산 로직을 별도 서비스로 유지해야 한다.

## 후속 작업

- `ProcessPlan` phase에서 “지금 해야 할 일” 계산을 실제 공정 계획 기반으로 교체한다.
- `VisualQuestion`, `ExpertLead` phase에서 placeholder 카드를 실제 최근 질문/추천 전문가 카드로 치환한다.
