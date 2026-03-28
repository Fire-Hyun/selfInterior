# ADR-0001: monorepo와 provider 전략 기반 Phase 1 vertical slice 채택

## 상태

승인

## 날짜

2026-03-28

## 맥락

설계서와 프롬프트 모음은 다음을 요구한다.

- monorepo 구조
- Next.js App Router + Spring Boot 조합
- 주소-first, 도면-fallback 구조
- provider adapter 뒤로 외부 API를 숨기는 전략
- provenance, `license_status`, `confidence_score`를 저장하는 도면 도메인
- 한 번에 전 영역을 만들지 않고 세로 슬라이스로 확장

현재 저장소는 문서만 존재하고 실행 코드가 없다. 또한 로컬에 JDK가 없어 Docker 기반 검증 경로를 병행해야 한다.

## 결정

다음 구조를 채택한다.

- `apps/web`: Next.js App Router 프론트엔드
- `apps/api`: Spring Boot API와 Flyway 마이그레이션
- `packages/shared-types`: 웹과 API가 공유하는 계약 타입
- `infra`: docker compose 기반 postgres/redis
- `docs`: phase/design/adr/issues/test-cases

첫 구현 단위는 다음 세로 슬라이스로 제한한다.

1. 주소 검색
2. 집 정보 resolve
3. 프로젝트 생성
4. 프로젝트에 속성 연결
5. 도면 후보 resolve와 저장

도면 확보와 속성 취합은 provider 전략 패턴을 따른다.

- 각 provider는 interface와 mock adapter를 가진다.
- 실제 연동은 추후 adapter 교체로 확장한다.
- provider 실패는 정상 흐름의 일부로 취급한다.

도면 후보 저장 시 아래 provenance 필드를 유지한다.

- `source`
- `license_status`
- `confidence`
- `raw_payload_ref`
- `normalized_plan_ref`

## 결과

긍정적 결과:

- 설계서 핵심 도메인과 흐름을 잃지 않는다.
- mock 기반으로 빠르게 실행 가능한 슬라이스를 확보할 수 있다.
- 실제 공공 API 연동과 고도화가 쉬워진다.

부정적 결과:

- 초기에는 mock 데이터와 실제 데이터 간 격차가 있다.
- Docker 기반 API 검증이 로컬 Java 실행보다 느릴 수 있다.

## 대안

### 프론트와 백엔드를 모두 TypeScript로 통일

배제 이유:

- 설계서와 프롬프트가 Spring Boot를 명시한다.

### 전체 DB 스키마를 한 번에 구현

배제 이유:

- 현재 목표는 실행 가능한 첫 vertical slice 확보이며, 한 번에 전 영역을 구축하면 위험이 크다.
