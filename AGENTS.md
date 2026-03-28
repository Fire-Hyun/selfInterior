# AGENTS.md

## 반드시 지킬 원칙

1. 구현 전에 먼저 설계 문서를 작성하거나 갱신한다.
2. 구조적 결정은 모두 `docs/adr/`에 남긴다.
3. 공식 및 공개 출처를 최우선으로 사용하고, 웹 검색은 보조 수단으로만 사용한다.
4. `README.md`와 `AGENTS.md`는 한글로 유지한다. 경로, 명령어, 코드 식별자만 원문 유지가 가능하다.
5. 비밀값, 세션 파일, API 키, 인증 정보, 브라우저 산출물은 커밋하지 않는다.
6. 각 작업 단위는 `format`, `lint`, `typecheck`, `test`, `docs:check`, `build`가 green이고 문서가 갱신된 뒤에만 끝낼 수 있다.
7. git 인증정보는 ../dev/config/.git 하단에 있는 PAT를 사용한다. remote 레포지토리가 없으면 생성한후 commit, push한다.
8. 완료된 작업 단위는 즉시 Conventional Commit으로 커밋하고, remote가 있으면 push를 시도한다.

## 작업 시작 전 체크리스트

1. `README.md`, `AGENTS.md`, 관련 설계 문서, phase 문서를 읽는다.
2. `git status --short --branch`를 확인한다.
3. `.env`와 `.env.example`를 읽고 키 상태를 기록한다.
4. 현재 단계 경계와 영향 파일을 먼저 정의한다.

## 문서 구조 규칙

- 단계 설계와 close-out 기준은 `docs/phases/phase-*.md`에 기록한다.
- 카테고리별 설계는 `docs/design/category-*.md`에 기록한다.
- 구조적 의사결정은 `docs/adr/ADR-*.md`에 기록한다.
- 이슈와 외부 차단 사항은 `docs/issues/YYYY-MM-DD-*.md`에 기록한다.
- 테스트 케이스는 `docs/test-cases/`에 유지한다.
- 런북과 운영 절차는 `docs/runbooks/`에 기록한다.
- 구현 범위나 운영 상태가 바뀌면 README도 같은 변경 안에서 갱신한다.

## 구현 경계

- `apps/web`: 사용자 웹 플로우와 App Router UI
- `apps/api`: Spring Boot API, 도메인 서비스, persistence, mock/real provider adapter
- `packages/shared-types`: 웹과 API가 공유하는 계약 타입
- `docs`: phase, 설계, ADR, 이슈, 테스트 케이스
- `infra`: 로컬 개발용 인프라 구성
- `scripts`: 검증, docs check, Docker 기반 보조 실행 스크립트

## 테스트 규칙

- 각 작업 단위는 최소한 `format`, `lint`, `typecheck`, `test`, `docs:check`, `build`를 통과하도록 유지한다.
- 로컬 도구 부재가 있으면 Docker 기반 대체 경로를 우선 준비한다.
- 테스트가 아직 없는 레이어라도 smoke test 또는 최소 통합 검증 경로를 남긴다.
- mock provider가 있는 모듈은 성공 경로와 fallback 경로를 모두 검증한다.
- 문서 변경이 동반된 작업은 `docs:check` 실패가 나지 않아야 한다.

## 비밀값 처리

- 추적 파일에는 placeholder만 둔다.
- 실제 값은 `.env.local` 또는 커밋되지 않는 로컬 비밀 저장소에만 둔다.
- `.env`, `.env.local`, `data/`, `output/`, `tmp/`, `logs/`, 브라우저 세션 파일은 `.gitignore`로 보호한다.
- 실값이 감지되면 문서에는 상태만 기록하고 값 자체는 절대 남기지 않는다.

## 커밋 및 푸시

- Conventional Commits를 사용한다.
- 한 번에 한 단계만 진행한다.
- green 상태가 되면 즉시 커밋한다.
- remote가 있으면 각 단계 종료 시 push를 시도한다.
- push 실패 시 원인과 재시도 방안을 README와 이슈 문서에 기록한다.

## 완료 기준

아래 조건을 모두 만족해야 작업 단위가 완료다.

1. 설계 문서가 구현 구조를 반영한다.
2. README가 현재 범위와 차단 사항을 반영한다.
3. 테스트와 문서 검증이 green이다.
4. 추적 파일에 비밀값이 없다.
5. 커밋과 push 시도 결과가 기록되었다.

## 현재 프로젝트 추가 원칙

- 핵심 도메인 `Project`, `Property`, `AddressResolution`, `FloorPlanCandidate`, `NormalizedPlan`, `ProcessPlan`, `VisualQuestion`, `ExpertLead`를 유지한다.
- 도면 확보는 provider 기반 전략 패턴으로 구현한다.
- provider 결과에는 `confidence`, `source`, `license_status`, `raw_payload_ref`, `normalized_plan_ref`를 남긴다.
- 공공 API는 실제 호출부와 mock adapter를 분리한다.
- 프론트/백/API/DB를 동시에 크게 확장하지 않고 세로 슬라이스 단위로 구현한다.
- 매 Phase마다 실행 가능한 상태를 유지한다.
