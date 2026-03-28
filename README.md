# selfInterior

`selfInterior`는 주소와 주거 속성을 바탕으로 도면, 공정, 사진 질문, 전문가 연결, 스타일 탐색까지 이어지는 셀프 인테리어 MVP 저장소다.

## 현재 상태

- 현재 단계: Phase 3.0 구현 완료, Phase 1.3 온보딩 개선 반영
- 현재 기본 온보딩: `단지명 검색 -> 평형 선택 -> 프로젝트 생성 -> 도면 후보 선택`
- 원격 저장소: [github.com/Fire-Hyun/selfInterior](https://github.com/Fire-Hyun/selfInterior)
- 최신 커밋: `d6e1b3d` `feat: switch onboarding to complex-first area flow`
- 최신 push: 2026-03-29 `origin/main` 반영 완료
- 기준 문서:
  - `interior_architecture_spec_ko.md`
  - `codex_prompts_ko.txt`
  - `docs/phases/phase-0.md`
  - `docs/phases/phase-1.md`
  - `docs/phases/phase-2.md`
  - `docs/phases/phase-3.md`

## 핵심 도메인

- `Project`
- `Property`
- `AddressResolution`
- `FloorPlanCandidate`
- `NormalizedPlan`
- `ProcessPlan`
- `VisualQuestion`
- `ExpertLead`

## 저장소 구조

- `apps/web`: Next.js App Router 기반 프론트엔드
- `apps/api`: Spring Boot API, Flyway 마이그레이션, provider adapter
- `packages/shared-types`: web과 api가 공유하는 DTO와 타입
- `docs`: phase, 설계, ADR, 이슈, 테스트 케이스, 런북
- `infra`: Docker Compose 기반 로컬 인프라
- `scripts`: 검증 및 로컬 실행 스크립트

## 구현 원칙

- 설계 문서를 먼저 갱신한 뒤 구현한다.
- 공공 API는 실제 호출부와 mock adapter를 분리한다.
- 도면 확보는 provider 전략 패턴으로 유지한다.
- 모든 도면 후보에 `confidence`, `source`, `license_status`, `raw_payload_ref`, `normalized_plan_ref`를 남긴다.
- 세로 슬라이스 방식으로 확장하고, 각 phase는 실행 가능한 상태를 유지한다.

## 로컬 실행

가장 쉬운 시작 방법은 아래 한 줄이다.

```bash
npm run dev:up
```

기본 모드는 Docker 우선이다.

- `postgres`, `redis`, `api`는 Docker Compose로 실행한다.
- web은 로컬 `next dev` 창으로 실행한다.
- 상태 확인: `npm run dev:status`
- 종료: `npm run dev:down`

상세 실행 절차는 [local-dev-quickstart.md](/Users/jun12/dev/selfInterior/docs/runbooks/local-dev-quickstart.md)에 있다.

## 검증 명령

루트에서 아래 명령을 실행한다.

```bash
npm run format
npm run lint
npm run typecheck
npm run test
npm run docs:check
npm run build
```

API 검증은 기본적으로 Docker `gradle` 컨테이너를 사용한다. 로컬 JDK를 강제로 쓰려면 `SELFINTERIOR_USE_LOCAL_JDK=1`을 설정한다.

## 현재 vertical slices

1. 단지 검색
2. 집 정보 resolve
3. 프로젝트 생성
4. 프로젝트-속성 연결
5. 도면 후보 resolve 및 선택
6. 프로젝트 홈 read model
7. 공정 플랜 생성과 상세 조회
8. 사진 질문 등록과 분석 결과 조회
9. 전문가 추천과 문의 생성
10. 스타일 preset 조회와 이미지 생성

## 환경 변수

실제 비밀값은 `.env.local` 또는 커밋되지 않는 로컬 저장소에만 둔다.

주요 키

- `OPENAI_API_KEY`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_URL`
- `SPRING_PROFILES_ACTIVE`
- `NEXT_PUBLIC_API_BASE_URL`

현재 상태

- `.env.example`는 placeholder만 포함한다.
- 루트 `.env`에 실제 비밀값이 감지된 상태이며 값은 기록하지 않는다.
- 관련 상태 기록은 [2026-03-28-local-env-and-secret-state.md](/Users/jun12/dev/selfInterior/docs/issues/2026-03-28-local-env-and-secret-state.md)에 있다.

## 최근 설계 변경

- [ADR-0007-complex-first-area-selection-onboarding.md](/Users/jun12/dev/selfInterior/docs/adr/ADR-0007-complex-first-area-selection-onboarding.md)
- [ADR-0008-docker-first-local-execution.md](/Users/jun12/dev/selfInterior/docs/adr/ADR-0008-docker-first-local-execution.md)

## 차단 사항

- Docker Desktop이 꺼져 있으면 기본 실행 경로가 동작하지 않는다.
- 루트 `.env`의 실제 비밀값은 별도 정리와 rotation이 필요하다.
