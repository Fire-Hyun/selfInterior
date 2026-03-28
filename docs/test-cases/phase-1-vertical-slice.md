# Phase 1 vertical slice 테스트 케이스

## 주소 검색

- 검색어로 주소 후보가 반환된다.
- 주소 provider 하나가 실패해도 fallback 후보가 반환된다.
- 결과에는 도로명 주소, 지번 주소, 좌표, 동 후보 정보가 포함된다.

## 집 정보 resolve

- 선택한 주소로 `Property` 요약이 반환된다.
- 외부 참조 목록이 반환된다.
- integration log가 남는다.

## 프로젝트 생성

- 프로젝트가 생성된다.
- 기본 `project_scope`가 함께 준비된다.

## 프로젝트-집 연결

- 프로젝트에 속성을 연결할 수 있다.
- 이후 도면 resolve 입력으로 사용할 수 있다.

## 도면 후보 resolve

- provider 전략이 실행된다.
- 후보가 저장된다.
- 각 후보에 `source`, `license_status`, `confidence`, `raw_payload_ref`, `normalized_plan_ref`가 기록된다.
