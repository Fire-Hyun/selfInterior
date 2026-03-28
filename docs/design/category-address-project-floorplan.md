# 주소-프로젝트-도면 후보 카테고리 설계

## 범위

이 문서는 첫 vertical slice에서 구현하는 `AddressResolution -> Property -> Project -> FloorPlanCandidate` 흐름의 카테고리 설계를 정의한다.

## 사용자 흐름

1. 홈에서 주소 또는 단지명을 검색한다.
2. 검색 결과에서 집 후보를 선택한다.
3. 동/호 등 상세 옵션을 보정한다.
4. 시스템이 `Property` 요약과 외부 참조를 resolve 한다.
5. 사용자가 프로젝트를 생성한다.
6. 프로젝트에 집 정보를 연결한다.
7. 시스템이 도면 provider 전략을 실행해 `FloorPlanCandidate`를 저장한다.
8. 사용자는 후보 리스트와 신뢰도, 출처, 라이선스 상태를 본다.
9. 사용자는 가장 적합한 후보를 선택하고 프로젝트 상세 요약을 확인한다.

## 도메인 모델 요약

### `AddressResolution`

- 입력 검색어
- 정규화 주소
- 좌표
- 도로명 코드
- 법정동 코드
- 신뢰도
- 원본 payload 참조

### `Property`

- 주소
- 아파트명
- 동/호
- 평형 후보
- 세대수
- 준공연도
- 외부 참조 목록

### `Project`

- 제목
- 프로젝트 유형
- 거주 상태
- 예산 범위
- 연결된 `Property`
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

### 웹

- 홈 검색 페이지
- 검색 결과 및 상세 옵션 선택
- 프로젝트 생성 폼
- 도면 후보 요약 화면
- 프로젝트 상세 상태 패널

### API

- controller: request/response 경계
- application service: use case orchestration
- domain service: provider 전략 및 fallback
- adapter: 외부 provider mock/real 구현
- persistence: JPA entity, repository, Flyway

## provider 전략

### 주소 전략

- 1차: `KakaoAddressClient`
- 2차: `JusoAddressClient`
- 실패 시 부분 결과를 유지하고 사용자 선택 가능한 후보를 반환한다.

### 속성 전략

- `KaptClient`로 단지 힌트 확보
- `BuildingHubClient`, `HousingHubClient`, `VworldClient`로 보강
- provider 실패는 integration log에 남기고 가능한 데이터만 조합한다.

### 도면 전략

1. `OfficialFloorPlanClient`
2. `LicensedFloorPlanClient`
3. 내부 근사 생성 fallback

선택 정책:

- 최초 resolve 시 최고 신뢰도 후보를 자동 선택한다.
- 사용자는 이후 후보를 수동 선택할 수 있다.
- 선택 변경 시 프로젝트 상세 응답이 같은 기준을 참조한다.

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

## 테스트 포인트

- 주소 검색 성공
- 주소 검색 fallback
- 속성 resolve 시 외부 참조 결합
- 프로젝트 생성 후 속성 연결
- 도면 resolve 시 후보 저장
- provider provenance 필드 저장
- 도면 후보 수동 선택 시 기존 선택 해제
- 프로젝트 상세 응답에 property/selected plan 요약 포함
