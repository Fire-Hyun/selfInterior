# ADR-0003 ProcessPlan은 카탈로그 seed를 프로젝트 snapshot으로 복제한다

## 상태

승인

## 배경

공정 플래너는 모든 프로젝트에 공통인 공정 지식과, 각 프로젝트에 종속적인 진행 상태를 함께 다뤄야 한다.
공정 제목, 목적, 체크리스트 템플릿은 공통 자산에 가깝지만, 완료 상태와 현재 단계는 프로젝트마다 달라진다.

## 결정

`ProcessPlan`은 아래 두 계층으로 나눈다.

- 공통 seed 계층: `process_catalogs`, `process_guides`, `process_checklists`
- 프로젝트 snapshot 계층: `project_process_plans`, `project_process_steps`, `project_process_tasks`

플랜 생성 시 공통 seed를 읽어 프로젝트별 snapshot으로 복제한다.

## 이유

- 공정 가이드 텍스트와 체크리스트 템플릿을 재사용할 수 있다.
- 프로젝트별 완료 상태를 seed 데이터와 분리해 안전하게 저장할 수 있다.
- 이후 관리자 CMS를 붙일 때 공통 카탈로그만 편집하면 된다.
- 프로젝트 생성 시점의 기준 공정을 snapshot으로 남겨 버전 영향을 줄일 수 있다.

## 결과

장점:

- 공통 지식과 프로젝트 실행 상태의 책임이 분리된다.
- 체크리스트 완료 토글이 간단해진다.
- 이후 관리자 기능과 seed 갱신이 쉬워진다.

단점:

- snapshot 생성 로직이 추가된다.
- seed 수정 후 기존 프로젝트와의 차이를 별도로 관리해야 한다.

## 후속 작업

- 관리자 기능에서 카탈로그/가이드 편집 UI를 추가한다.
- 버전 관리가 필요해지면 `catalog_version` 개념을 도입한다.
