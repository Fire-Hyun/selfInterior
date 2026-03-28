# 주소-프로젝트-도면 후보 카테고리 설계

## 범위

이 문서는 초기 vertical slice에서 구현하는 `AddressResolution -> Property -> Project -> FloorPlanCandidate` 흐름을 정의한다.

## 기본 사용자 흐름

1. 사용자는 정확한 동/호가 아니라 아파트 단지명 또는 도로명 수준으로 검색한다.
2. 검색 결과에서 단지 후보를 선택한다.
3. 시스템이 선택된 단지의 `Property` 요약과 평형 후보를 resolve 한다.
4. 사용자는 평형 버튼 또는 메뉴 중 하나를 선택한다.
5. 사용자는 프로젝트를 생성한다.
6. 프로젝트에 선택한 단지와 평형 정보를 연결한다.
7. 시스템이 provider 전략으로 `FloorPlanCandidate`를 수집한다.
8. 사용자는 출처, 라이선스, 신뢰도를 보고 도면 후보를 선택한다.
9. 선택된 도면 후보가 프로젝트 상세와 홈 read model에 반영된다.

## 도메인 요약

### `AddressResolution`

- 입력 검색어
- 정규화된 단지명
- 도로명 주소
- 지번 주소
- 좌표
- 도로명 코드
- 법정동 코드
- 검색 provenance
- raw payload 참조

### `Property`

- 단지명
- 주소
- 준공 연도
- 세대 수
- 평형 후보
- 방/욕실 후보
- 외부 참조 목록

### `Project`

- 제목
- 프로젝트 유형
- 거주 상태
- 예산 범위
- 연결된 `Property`
- 사용자가 선택한 평형
- 선택된 `FloorPlanCandidate`

### `FloorPlanCandidate`

- source/provider
- license_status
- confidence_score
- confidence_grade
- match_type
- raw_payload_ref
- normalized_plan_ref

## 애플리케이션 계층

### Web

- 단지 검색 입력
- 검색 결과 목록
- 단지 선택 후 평형 버튼 표시
- 프로젝트 생성 CTA
- 도면 후보 목록 및 선택
- 프로젝트 홈 진입

### API

- controller: 요청/응답 경계
- application service: 검색, resolve, 프로젝트 생성 orchestration
- provider adapter: 주소/단지 정보/도면 후보 mock 또는 real 구현
- persistence: JPA entity, repository, Flyway
- read model: 프로젝트 홈과 상세 응답 조합

## provider 전략

### 주소 검색

- 1차 `KakaoAddressClient`
- 2차 `JusoAddressClient`
- 검색 실패 시 fallback 하되, 결과는 단지 후보 단위로 노출한다.

### 속성 resolve

- `KaptClient`로 단지 요약과 평형 후보를 조회한다.
- `BuildingHubClient`, `HousingHubClient`, `VworldClient`로 외부 참조와 지번 주소를 보강한다.
- 기본 온보딩에서는 동/호 없이 단지와 평형만 확정한다.
- 상세 주소는 이후 phase의 보정 흐름으로 분리한다.

### 도면 확보

1. `OfficialFloorPlanClient`
2. `LicensedFloorPlanClient`
3. `ApproximateFloorPlanGenerator`

모든 후보에 대해 다음 provenance를 남긴다.

- `confidence`
- `source`
- `license_status`
- `raw_payload_ref`
- `normalized_plan_ref`

## persistence 최소 테이블

- `projects`
- `project_members`
- `properties`
- `address_resolution_logs`
- `external_property_refs`
- `floor_plan_sources`
- `floor_plan_candidates`
- `normalized_floor_plans`
- `integration_call_logs`

## 테스트 기준

- 단지명 검색 성공
- 주소 provider fallback 동작
- 단지 선택 후 평형 후보 resolve
- 평형 선택 뒤 프로젝트 생성과 속성 연결
- 도면 후보 resolve 시 provenance 필드 기록
- 도면 후보 수동 선택 시 기존 선택 해제
- 프로젝트 상세 응답에 선택 평형과 선택 도면 포함
- 프로젝트 홈 응답에 집 요약과 다음 액션 포함
