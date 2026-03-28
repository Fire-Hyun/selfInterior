# Phase 1 vertical slice 테스트 케이스

## 단지 검색

- 단지명 일부만 입력해도 후보가 반환된다.
- 1차 주소 provider가 비어 있어도 fallback 결과가 반환된다.
- 결과에는 단지명, 도로명 주소, 좌표, 평형 힌트가 포함된다.

## 집 정보 resolve

- 선택한 단지로 `Property` 요약이 반환된다.
- 평형 버튼에 사용할 `areaOptions`가 반환된다.
- 외부 참조 목록과 integration log가 기록된다.

## 프로젝트 생성

- 프로젝트가 생성된다.
- 선택한 평형으로 `Property`가 연결된다.
- 이후 도면 resolve 입력으로 사용된다.

## 도면 후보 resolve

- provider 전략이 실행된다.
- 후보가 저장된다.
- 각 후보에 `source`, `license_status`, `confidence`, `raw_payload_ref`, `normalized_plan_ref`가 남는다.

## 프로젝트 홈과 상세

- 상세 응답에 집 요약과 선택 평형이 포함된다.
- 도면 후보 선택 결과가 프로젝트 상세와 홈에 반영된다.
- `/projects/{projectId}/home`으로 진입 가능하다.

## 로컬 실행

- `npm run dev:up` 기본 동작으로 Docker Compose의 `postgres`, `redis`, `api`가 실행된다.
- web은 로컬 창에서 실행된다.
- `npm run dev:status`로 상태를 확인할 수 있다.
