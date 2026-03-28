# Phase 0 정리

## 목적

설계 문서 기준선을 고정하고, 첫 구현 슬라이스의 범위와 구조를 명확히 정의한다.

## 읽은 기준 문서

- `interior_architecture_spec_ko.md`
- `codex_prompts_ko.txt`
- `AGENTS.md`

## 요구사항 구조화

### 제품 흐름

1. 사용자가 주소 또는 아파트명/동·호를 입력한다.
2. 시스템이 `AddressResolution`을 수행해 표준 주소와 단지 후보를 찾는다.
3. 선택된 주소를 기준으로 `Property` 메타데이터를 취합한다.
4. 사용자가 `Project`를 생성하고 집 정보를 연결한다.
5. 시스템이 provider 전략으로 `FloorPlanCandidate`를 확보하고 저장한다.
6. 이후 `NormalizedPlan`, `ProcessPlan`, `VisualQuestion`, `ExpertLead`로 확장한다.

### 이번 구현에 직접 필요한 도메인

- `Project`
- `Property`
- `AddressResolution`
- `FloorPlanCandidate`

### 후속 Phase에서 이어질 도메인

- `NormalizedPlan`
- `ProcessPlan`
- `VisualQuestion`
- `ExpertLead`

## 현재 저장소 구조 스캔 결과

- 루트 문서만 존재하고 코드/문서/인프라 구조는 비어 있다.
- `README.md` 부재
- `docs/` 부재
- `apps/web`, `apps/api`, `packages/shared-types`, `infra` 부재
- `.env.example` 부재
- 로컬 `.env`에 실제 비밀값 존재
- 로컬 `java`, `gradle` 부재
- `docker` 사용 가능

## 설계 대비 누락/충돌 사항

### 누락

- monorepo 구조 전체
- 공통 응답 포맷과 requestId/logging
- Flyway 기반 DB 마이그레이션
- provider adapter 인터페이스와 mock 구현
- 주소 검색, 프로젝트 생성, 도면 후보 저장 API
- 주소 검색과 프로젝트 생성 UI
- 문서 체계와 ADR

### 충돌 또는 정리 필요

- 기존 `AGENTS.md`의 구현 경계가 현재 프로젝트의 monorepo 구조와 맞지 않았다.
- 로컬 JDK가 없어 API 검증 경로를 Docker 기반으로 우회해야 한다.
- 실제 비밀값이 `.env`에 존재하므로 추적 차단과 문서화가 필요하다.

## Phase 1 범위 정의

### 목표

주소 검색부터 프로젝트 생성, 집 정보 연결, 도면 후보 저장까지 한 번에 이어지는 최소 실행 슬라이스를 만든다.

### 포함 범위

- monorepo 부트스트랩
- `apps/web` 기본 UI
- `apps/api` 기본 API
- `packages/shared-types` DTO/도메인 계약
- `properties`, `projects`, `address_resolution_logs`, `external_property_refs`, `floor_plan_sources`, `floor_plan_candidates`, `normalized_floor_plans`, `integration_call_logs` 중심의 초기 마이그레이션
- mock provider 기반 주소 검색/resolve
- mock provider 기반 도면 후보 저장

### 제외 범위

- 인증/인가 본격 구현
- 스타일 이미지 생성
- 공정 플래너 생성
- 사진 질문답변
- 전문가 리드 생성
- 실제 공공 API 연동

## 첫 vertical slice 설계 요약

### API

- `POST /api/v1/address/search`
- `POST /api/v1/address/detail-options`
- `POST /api/v1/property/resolve`
- `POST /api/v1/projects`
- `GET /api/v1/projects`
- `GET /api/v1/projects/{projectId}`
- `POST /api/v1/projects/{projectId}/property`
- `POST /api/v1/projects/{projectId}/floor-plans/resolve`
- `GET /api/v1/projects/{projectId}/floor-plans`

### provider 전략

- 주소 계층:
  - `KakaoAddressClient`
  - `JusoAddressClient`
- 속성 계층:
  - `KaptClient`
  - `BuildingHubClient`
  - `HousingHubClient`
  - `VworldClient`
- 도면 계층:
  - `OfficialFloorPlanClient`
  - `LicensedFloorPlanClient`

모든 외부 클라이언트는 interface와 mock 구현을 분리한다.

### 도면 provenance 최소 저장 정책

- `source`
- `license_status`
- `confidence`
- `raw_payload_ref`
- `normalized_plan_ref`

## 완료 기준

- 문서 구조가 생성되어 있다.
- README와 AGENTS가 현재 구조를 설명한다.
- monorepo와 첫 vertical slice 코드가 생성된다.
- 최소 검증 명령이 준비된다.
- 비밀값 추적 방지 설정이 반영된다.
