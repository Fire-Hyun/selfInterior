# ADR-0008 Docker 우선 로컬 실행 경로

## 상태

승인

## 배경

로컬 실행 시 Java 설치 여부와 `JAVA_HOME` 설정이 자주 장애가 되었다. 반면 이 프로젝트는 이미 `postgres`, `redis`를 Docker로 올리고 있고, API도 컨테이너에서 충분히 실행 가능하다.

## 결정

기본 로컬 실행 경로를 Docker 우선으로 통일한다.

- `npm run dev:up` 기본 동작은 `postgres`, `redis`, `api`를 Docker Compose로 실행한다.
- web은 로컬 `next dev`로 실행한다.
- API 검증 스크립트는 기본적으로 Docker `gradle` 이미지를 사용한다.
- 필요할 때만 `SELFINTERIOR_USE_LOCAL_API=1`, `SELFINTERIOR_USE_LOCAL_JDK=1`로 로컬 JDK 경로를 사용한다.

## 결과

- 신규 환경에서 실행 마찰이 줄어든다.
- 로컬 JDK 부재가 기본 실행 차단 요인이 되지 않는다.
- Docker Desktop이 켜져 있으면 같은 명령으로 팀 공통 실행이 가능해진다.
