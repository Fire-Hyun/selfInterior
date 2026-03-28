# Phase 1 구현 계획

## 목적

주소/단지 온보딩의 첫 실행 단위를 실제 사용 흐름으로 닫는다.

## Phase 1 범위

### Phase 1.0

- monorepo 부트스트랩
- 주소 검색
- 집 정보 resolve
- 프로젝트 생성
- 프로젝트-속성 연결
- 도면 후보 저장과 조회

### Phase 1.1

- 프로젝트 상세 응답에 집 요약과 선택된 도면 요약 포함
- 도면 후보 선택 API 추가
- web에서 생성된 프로젝트의 현재 상태 확인

### Phase 1.2

- `/projects/{projectId}/home` read model 추가
- 집 요약, 도면 요약, 다음 액션, placeholder 카드 구성
- `ProcessPlan`, `VisualQuestion`, `ExpertLead`를 이후 phase와 연결할 자리 확보

### Phase 1.3

- 기본 온보딩을 `단지 검색 -> 평형 선택 -> 프로젝트 생성`으로 전환
- 동/호 입력 제거
- 단지 검색 결과에 평형 힌트 노출
- 선택한 평형을 프로젝트 속성에 저장
- 로컬 실행 기본 경로를 Docker 우선으로 정리

## Phase 1.3 구현 상세

### 설계

- 단지 선택 후 `Property` resolve 응답에서 평형 옵션을 바로 받는다.
- 평형은 버튼 또는 메뉴로 노출한다.
- 동/호는 기본 온보딩에서 받지 않는다.
- `AttachPropertyRequest`에는 사용자가 선택한 평형만 저장한다.

### API

- `POST /api/v1/address/search`
  - 단지명 검색 결과를 반환한다.
- `POST /api/v1/property/resolve`
  - 선택한 단지의 평형 후보를 포함한 `Property` 요약을 반환한다.
- `POST /api/v1/projects/{projectId}/property`
  - 선택한 평형으로 속성을 연결한다.
- `POST /api/v1/projects/{projectId}/floor-plans/resolve`
  - 평형 기반 도면 후보를 수집한다.

### Web

- 검색 입력은 단지명 중심 placeholder로 바꾼다.
- 검색 결과 카드에서 단지 규모와 평형 힌트를 보여준다.
- 단지 선택 즉시 평형 버튼이 나타난다.
- 평형 선택 뒤 프로젝트 생성 CTA가 활성화된다.

### 실행

- `npm run dev:up`은 기본적으로 Docker Compose로 `postgres`, `redis`, `api`를 함께 실행한다.
- web만 로컬 `npm run dev` 창으로 올린다.
- API 검증 명령은 기본적으로 Docker `gradle` 컨테이너를 사용한다.

## 완료 기준

- 설계 문서와 ADR이 새 온보딩 흐름을 반영한다.
- 단지명만으로 검색하고 평형 선택 뒤 프로젝트 생성이 가능하다.
- 도면 후보 resolve와 선택 흐름이 유지된다.
- `format`, `lint`, `typecheck`, `test`, `docs:check`, `build`가 green이다.
- 커밋과 push 시도 결과가 기록된다.
