# ExpertLead 카테고리 설계

## 범위

이 문서는 `ExpertLead` vertical slice에서 구현하는 전문가 카테고리/목록 조회, 프로젝트 기반 추천, 전문가 리드 생성, 프로젝트 홈 추천 전문가 카드 연동을 정의한다.

## 사용자 흐름

1. 사용자는 프로젝트 홈 또는 사진 질문 화면에서 전문가 추천 화면으로 이동한다.
2. 시스템은 프로젝트 예산, 주소 지역, 현재 공정, 최근 질문 위험도를 반영해 추천 카테고리와 전문가 목록을 계산한다.
3. 사용자는 전문가 상세를 확인하고 문의 메시지를 작성한다.
4. 시스템은 프로젝트 요약, 최근 질문 요약, 관련 이미지 경로를 `attachment_payload`에 포함한 리드를 저장한다.
5. 프로젝트 홈은 가장 적합한 추천 전문가를 카드로 노출한다.

## 도메인 모델 요약

### `ExpertCategory`

- `key`
- `name`
- `active`

### `Expert`

- `company_name`
- `contact_name`
- `intro_text`
- `min_budget`
- `max_budget`
- `partial_work_supported`
- `semi_self_collaboration_supported`
- `response_score`
- `review_score`
- `status`

### `ExpertServiceRegion`

- `expert_id`
- `sido`
- `sigungu`

### `ExpertPortfolio`

- `expert_id`
- `title`
- `description`
- `storage_key`
- `metadata`

### `ExpertLead`

- `project_id`
- `expert_id`
- `requested_category_id`
- `lead_status`
- `budget_min`
- `budget_max`
- `desired_start_date`
- `message`
- `attachment_payload`
- `created_by_user_id`

### `ExpertLeadEvent`

- `expert_lead_id`
- `event_type`
- `payload`

## 추천 규칙

- 기본 입력은 `Project`, `Property`, `ProcessPlan`, 최근 `VisualQuestion` 답변이다.
- 최근 질문이 `HIGH` 이상이면 risk level과 질문 문맥을 우선 반영한다.
- 질문 기반 우선 카테고리가 없으면 현재 공정 단계 기반 기본 카테고리를 사용한다.
- 전문가 노출 대상은 `ACTIVE` 상태만 허용한다.
- 지역은 `Property.sido`, `Property.sigungu`와 일치하는 전문가를 우선한다.
- 예산은 프로젝트 예산과 전문가 예산 범위가 겹치는 전문가를 우선한다.
- 추천 결과는 1순위 카테고리, 2순위 카테고리, 전문가 목록, 추천 점수를 포함한다.

## attachment payload 규칙

- 리드 생성 시 아래 정보를 JSON으로 저장한다.
- 프로젝트 제목, 예산, 현재 공정 단계
- 집 주소 요약과 면적 요약
- 최근 질문의 위험도, 질문 텍스트, 관련 이미지 경로
- 사용자가 입력한 메시지와 요청 카테고리

## 애플리케이션 계층

### API

- controller: 카테고리/전문가 조회, 프로젝트별 추천 조회, 리드 생성
- application service: 추천 계산, 전문가 상세 조합, 리드 payload 생성
- adapter: 이번 단계에서는 별도 외부 연동 없이 seed 데이터와 내부 계산만 사용

### Web

- 프로젝트별 전문가 추천 route
- 추천 전문가 카드
- 전문가 상세 패널
- 리드 생성 폼

## persistence 최소 테이블

- `expert_categories`
- `experts`
- `expert_category_links`
- `expert_service_regions`
- `expert_portfolios`
- `expert_leads`
- `expert_lead_events`

## 테스트 포인트

- `ACTIVE` 전문가만 조회된다.
- 지역/예산/카테고리 필터가 적용된다.
- 프로젝트 추천이 최근 질문 위험도와 현재 공정을 반영한다.
- 리드 생성 시 `attachment_payload`에 프로젝트/질문 요약이 저장된다.
- 프로젝트 홈이 placeholder 대신 실제 추천 전문가 카드를 표시한다.
