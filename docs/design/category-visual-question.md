# VisualQuestion 카테고리 설계

## 범위

이 문서는 `VisualQuestion` vertical slice에서 구현하는 사진 질문 등록, 이미지 저장, 구조화된 답변, 프로젝트 홈 최근 질문 카드 연동을 정의한다.

## 사용자 흐름

1. 사용자는 프로젝트에서 현재 공정이나 공간을 기준으로 사진 질문을 등록한다.
2. 시스템은 이미지를 mock storage에 저장한다.
3. 시스템은 `VisionQaClient` mock 분석을 실행해 구조화된 답변을 생성한다.
4. 답변에는 risk level, 추가 확인 사항, 공정 진행 권고, 전문가 필요 여부가 포함된다.
5. 사용자는 질문 목록과 상세를 조회한다.
6. 프로젝트 홈은 가장 최근 질문을 카드로 표시한다.

## 도메인 모델 요약

### `VisualQuestion`

- `project_id`
- `question_text`
- `process_step_key`
- `space_type`
- `status`

### `VisualQuestionImage`

- `question_id`
- `file_name`
- `content_type`
- `storage_path`

### `VisualAnswer`

- `question_id`
- `risk_level`
- `observed_text`
- `possible_causes_text`
- `next_checks_text`
- `proceed_recommendation_text`
- `expert_required`
- `confidence_score`

## 애플리케이션 계층

### API

- controller: multipart 질문 등록, 목록, 상세 조회
- application service: 질문 저장, 이미지 저장, mock 분석, 답변 저장
- adapter: `VisionQaClient`, `VisualQuestionStorage`

### Web

- 사진 업로드 폼
- 질문 목록
- 질문 상세 답변 카드

## 생성 규칙

- 질문 등록은 프로젝트 단위로 수행한다.
- `process_step_key`가 비어 있으면 현재 프로젝트 공정 단계 또는 사용자 입력값으로 보정한다.
- 답변은 동기 mock 분석으로 먼저 저장하되 `status` 필드는 추후 비동기 전환 가능하도록 유지한다.
- `HIGH` 이상 risk level은 `expert_required=true`로 저장한다.

## persistence 최소 테이블

- `visual_questions`
- `visual_question_images`
- `visual_answers`

## 테스트 포인트

- multipart 질문 등록이 성공한다.
- 이미지 메타데이터와 storage path가 저장된다.
- 구조화된 답변이 저장되고 상세 조회에서 반환된다.
- 프로젝트 홈이 최근 질문 카드를 실제 데이터로 표시한다.
