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

## 이번 작업 단위 상세

### 설계

- `GET /api/v1/projects/{projectId}`는 아래를 함께 반환한다.
  - 프로젝트 기본 정보
  - 연결된 `Property` 요약
  - 선택된 `FloorPlanCandidate` 요약
  - 후보 개수

### API

- `POST /api/v1/projects/{projectId}/floor-plans/{candidateId}/select`
- 선택 시 동일 프로젝트의 다른 후보는 `is_selected=false`로 정리한다.
- 선택 이유는 request body의 `reason`을 저장한다.

### Web

- 프로젝트 생성 후 상세 패널 표시
- 도면 후보 카드에 선택 버튼 표시
- 선택 이후 상세 패널과 후보 목록 동기화

## 완료 기준

- 설계 문서가 구현 구조를 반영한다.
- 프로젝트 상세 응답으로 property/floor plan 상태를 한 번에 볼 수 있다.
- 사용자가 후보 선택을 변경할 수 있다.
- `format`, `lint`, `typecheck`, `test`, `docs:check`, `build`가 green이다.
- 커밋 및 push 시도 결과가 기록된다.
