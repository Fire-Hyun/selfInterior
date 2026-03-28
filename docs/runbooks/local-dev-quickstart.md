# 로컬 실행 빠른 시작

## 목적

Windows PowerShell 기준으로 `selfInterior`를 한 번에 실행하고 중지하는 명령을 정리한다.

## 시작

루트 [selfInterior](/Users/jun12/dev/selfInterior)에서 실행한다.

```powershell
npm run dev:up
```

이 스크립트는 아래를 자동으로 처리한다.

- Docker 엔진 실행 여부 확인
- `postgres`, `redis` 컨테이너 시작
- `.tools/jdk-*` portable JDK 탐색 및 `JAVA_HOME` 설정
- API `bootRun` 창 실행
- web `npm run dev` 창 실행

## 상태 확인

```powershell
npm run dev:status
```

확인 포인트:

- Web: `http://localhost:3000`
- API Health: `http://localhost:8080/actuator/health`

## 종료

```powershell
npm run dev:down
```

이 스크립트는 `dev:up`이 띄운 PowerShell 창을 종료하고 Docker 인프라도 내린다.

## 주의

- Docker Desktop이 꺼져 있으면 `dev:up`이 바로 실패한다.
- `.tools/jdk-*` 경로가 없으면 portable JDK 경로를 먼저 준비해야 한다.
- 이미 3000/8080 포트가 사용 중이면 새 창을 띄우지 않고 기존 실행을 유지한다.
