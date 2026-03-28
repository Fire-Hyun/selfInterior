# 2026-03-28 로컬 환경 및 비밀값 상태

## 요약

Phase 0 점검 중 아래 상태를 확인했다.

## 확인 사항

- 루트 `.env` 파일에 실제 비밀값이 존재한다.
- `.env.example`은 없었다.
- 로컬에 `java`, `gradle` 명령이 없다.
- `docker`는 사용 가능하다.
- git remote는 아직 연결되지 않았다.
- 현재 git 루트는 `C:\Users\jun12\dev`이며 `selfInterior`는 그 하위 경로다.
- GitHub 계정 `Fire-Hyun`을 PAT로 확인했고 `selfInterior` 원격 저장소를 생성했다.

## 영향

- `.env`는 반드시 `.gitignore`로 보호해야 한다.
- 문서에는 비밀값의 존재 여부만 기록하고 값 자체는 남기지 않는다.
- API 검증은 Docker 기반 Gradle 경로를 우선 사용한다.
- push 자동화는 remote 연결 전까지 보류된다.
- 상위 작업트리에는 형제 프로젝트가 함께 있어 무분별한 commit이 위험하다.
- 그래서 `selfInterior` 내부에 독립 git 저장소를 초기화해 원격과 연결하는 방식으로 전환한다.

## 대응

- `.gitignore`에 `.env`, `.env.local`, 로그/산출물 경로를 추가한다.
- `.env.example`에는 placeholder만 둔다.
- README에 현재 차단 상태를 반영한다.
- 추후 remote 연결 후 commit/push 정책을 재개한다.
- 이번 턴에서는 상위 repo 범위 충돌 위험 때문에 commit/push를 보류하고 문서에 이유를 남긴다.
- 원격 저장소 URL: `https://github.com/Fire-Hyun/selfInterior.git`
