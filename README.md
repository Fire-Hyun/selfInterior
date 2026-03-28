# selfInterior

`selfInterior`는 주소 기반으로 집 정보를 식별하고, 도면 후보를 확보한 뒤 반셀프 인테리어 실행 흐름으로 연결하는 웹 MVP 저장소다.

## 현재 상태

- 현재 단계: Phase 0 완료, Phase 3.0 구현 완료
- 이번 범위: 주소 검색, 프로젝트 생성, 프로젝트-속성 연결, 도면 후보 저장과 선택, 프로젝트 홈 대시보드, 공정 플래너, 사진 질문, 전문가 추천/문의, 스타일 카드 생성까지
- 원격 저장소: `https://github.com/Fire-Hyun/selfInterior.git`
- 최근 커밋: `fb4295c` `feat: add expert lead vertical slice`
- 최근 push: 2026-03-28 `fb4295c` `origin/main` push 성공
- 설계 기준 문서:
  - `interior_architecture_spec_ko.md`
  - `codex_prompts_ko.txt`
  - `docs/phases/phase-0.md`
  - `docs/phases/phase-1.md`
  - `docs/phases/phase-2.md`
  - `docs/phases/phase-3.md`
  - `docs/design/category-address-project-floorplan.md`
  - `docs/design/category-style-generation.md`
  - `docs/design/category-process-plan.md`
  - `docs/design/category-visual-question.md`
  - `docs/design/category-expert-lead.md`
  - `docs/adr/ADR-0001-monorepo-provider-slice.md`
  - `docs/adr/ADR-0002-project-home-read-model.md`
  - `docs/adr/ADR-0003-process-plan-catalog-snapshot.md`
  - `docs/adr/ADR-0004-visual-question-sync-mock-analysis.md`
  - `docs/adr/ADR-0005-expert-recommendation-project-signals.md`
  - `docs/adr/ADR-0006-style-image-mock-provider.md`

## 저장소 구조

- `apps/web`: Next.js App Router 기반 웹 프론트엔드
- `apps/api`: Spring Boot API, Flyway 마이그레이션, mock provider 어댑터
- `packages/shared-types`: 웹과 API가 공유하는 DTO/도메인 타입
- `docs`: 단계 문서, 설계, ADR, 이슈, 테스트 케이스
- `infra`: 로컬 개발용 인프라 설정
- `scripts`: 루트 검증 및 Docker 기반 API 작업 스크립트

## 핵심 도메인

아래 핵심 도메인은 설계서 기준으로 유지한다.

- `Project`
- `Property`
- `AddressResolution`
- `FloorPlanCandidate`
- `NormalizedPlan`
- `ProcessPlan`
- `VisualQuestion`
- `ExpertLead`

## 구현 원칙

- 주소-first, 도면-fallback 흐름을 유지한다.
- 도면 확보는 provider 전략 패턴으로 설계한다.
- provider 결과에는 `confidence`, `source`, `license_status`, `raw_payload_ref`, `normalized_plan_ref`를 남긴다.
- 공공 API는 실제 호출 어댑터와 mock 어댑터를 분리한다.
- 한 번에 전 영역을 동시에 만들지 않고 세로 슬라이스로 확장한다.
- 각 phase는 실행 가능한 상태를 유지한다.

## 환경 변수

실제 비밀값은 `.env.local` 또는 커밋되지 않는 로컬 저장소에만 둔다.

주요 키:

- `OPENAI_API_KEY`: 로컬 전용, 커밋 금지
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_URL`
- `SPRING_PROFILES_ACTIVE`
- `NEXT_PUBLIC_API_BASE_URL`

현재 로컬 상태:

- `.env.example`은 새로 제공한다.
- 루트 `.env`에는 실제 비밀값이 감지되었으며 추적 금지 상태를 유지해야 한다.
- 상세 기록은 `docs/issues/2026-03-28-local-env-and-secret-state.md`에 남긴다.

## 개발 명령

루트에서 실행한다.

```bash
npm install
npm run format
npm run lint
npm run typecheck
npm run test
npm run docs:check
npm run build
```

API 검증은 로컬 JDK가 없을 때 Docker 기반 Gradle 실행으로 처리한다.

로컬 인프라:

```bash
docker compose up -d postgres redis
```

## 이번 vertical slice

1. 주소 검색
2. 집 정보 resolve
3. 프로젝트 생성
4. 프로젝트에 집 정보 연결
5. provider 기반 도면 후보 저장 및 조회
6. 선택된 도면 후보를 프로젝트 상세에 반영
7. 프로젝트 홈 카드로 다음 행동을 확인
8. 공정 플랜 생성, 단계 상세 조회, 체크리스트 토글
9. 사진 질문 등록, 구조화된 답변 저장, 최근 질문 카드 연동
10. 전문가 추천 조회, 전문가 상세, 프로젝트 기반 문의 리드 생성
11. 스타일 preset 조회, 스타일 카드 생성, 좋아요 기반 선택 저장

## 차단 사항

- 현재 로컬에 `java`, `gradle`이 없다.
- 따라서 API 빌드와 테스트는 Docker 기반 경로를 우선 사용한다.
- portable JDK를 `.tools/`에 내려받아 로컬 Gradle 검증 경로를 보완했다.

## 문서 검증 기준

- Phase 문서와 README가 현재 범위를 반영해야 한다.
- ADR이 구조적 결정을 설명해야 한다.
- 비밀값은 추적 파일에 포함되면 안 된다.
