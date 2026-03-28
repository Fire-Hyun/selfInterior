# 로컬 실행 빠른 시작

## 목적

Windows PowerShell 기준으로 `selfInterior`를 가장 적은 준비로 실행하고 종료하는 방법을 정리한다.

## 기본 시작

루트 [selfInterior](/Users/jun12/dev/selfInterior)에서 실행한다.

```powershell
npm run dev:up
```

기본 모드는 Docker 우선이다.

- `postgres`, `redis`, `api`는 Docker Compose로 실행한다.
- web은 별도 PowerShell 창에서 `npm run dev`로 실행한다.
- 로컬 JDK가 없어도 기본 경로는 동작한다.
- 예전 로컬 `java` API가 8080을 잡고 있으면 같은 workspace 프로세스는 자동으로 정리한다.
- Docker API와 검증 명령은 Gradle project cache를 분리해서 서로 락 충돌이 나지 않게 한다.

## 상태 확인

```powershell
npm run dev:status
```

확인 포인트

- Web: `http://localhost:3000`
- API Health: `http://localhost:8080/actuator/health`
- Docker Compose 상태

## 종료

```powershell
npm run dev:down
```

이 명령은 `dev:up`이 띄운 web 창을 종료하고 Docker Compose 서비스도 내린다.

## 선택적 로컬 API 모드

정말 필요할 때만 아래처럼 로컬 JDK 기반 API 실행으로 바꾼다.

```powershell
$env:SELFINTERIOR_USE_LOCAL_API="1"
npm run dev:up
```

API 검증 명령도 기본적으로 Docker를 사용하며, 필요할 때만 아래 환경변수로 로컬 JDK를 강제한다.

```powershell
$env:SELFINTERIOR_USE_LOCAL_JDK="1"
npm run test
```

## 주의

- Docker Desktop이 꺼져 있으면 `dev:up`이 바로 실패한다.
- 루트 `.env`에는 실제 비밀값이 들어 있지 않도록 유지해야 한다.
- 포트 3000, 8080, 5432, 6379가 이미 사용 중이면 기존 프로세스를 먼저 확인한다.
