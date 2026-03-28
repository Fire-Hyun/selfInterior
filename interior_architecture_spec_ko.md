# 반셀프인테리어 웹 서비스 상세 설계서 (웹 MVP 기준)

작성 기준일: 2026-03-26  
목표: **수익성**과 **대중성**을 동시에 확보하는 웹 기반 반셀프인테리어 서비스 설계  
핵심 원칙: **주소-first, 실행-first, 매출-first**

---

## 0. 제품 한 줄 정의

사용자가 **아파트명/주소/동·호**를 입력하면,

1. 집 정보를 자동 식별하고,
2. 가능한 범위 내에서 도면을 자동 확보하거나 근사 평면을 생성하고,
3. 그 집 기준으로 스타일 예시, 공정 순서, 체크리스트, 사진 질문답변, 전문가 연결까지 이어주는
   **주소 기반 반셀프 인테리어 실행 플랫폼**을 만든다.

---

## 1. 가장 중요한 결론: “주소 → 도면 자동 확보”는 일부 가능하지만, 100% 보장형으로 설계하면 안 된다

### 1-1. 반드시 가져가야 할 전제

- 주소 정규화, 좌표 변환, 동/층/호 선택, 공동주택/건물 메타데이터 조회는 공공/민간 API 조합으로 충분히 가능하다.
- 다만 **개별 세대 도면을 전국 단위로 100% 자동 확보**하는 것은 공공 API만으로 안정적으로 보장하기 어렵다.
- 따라서 백엔드는 **도면 확보 실패를 전제로 한 fallback 구조**를 가져야 한다.

### 1-2. 제품/기술 의사결정

이 서비스는 도면 획득을 아래 4단계로 설계한다.

1. **정확 도면**
   - 공식 승인 소스 또는 정식 라이선스 민간 소스에서 획득
2. **유사 도면**
   - 동일 단지 + 동일/유사 평형 + 동일 타입 기반 후보
3. **근사 평면**
   - 면적, 방수, 욕실수, 발코니, 주방 위치 기반 생성
4. **사용자 확인 보정**
   - “이 3개 중 어떤 구조가 제일 비슷한가요?” 선택

즉 서비스 UX는  
“도면 없으면 실패”가 아니라  
**“최대한 자동으로 만들고, 마지막 확인만 사용자에게 받는 구조”**여야 한다.

---

## 2. 외부 데이터 소스 설계

## 2-1. 주소/건물 식별 계층

### A. 주소 정규화 계층

역할:

- 주소 문자열 정규화
- 지번/도로명 주소 상호 변환
- 좌표 확보
- 검색 UX 제공

필요 어댑터:

- `KakaoAddressAdapter`
- `JusoDetailAddressAdapter`

사용 목적:

- 사용자가 “잠실 리센츠 201동 1203호”처럼 입력해도 표준화
- 동/층/호 후보를 자동 선택 또는 보조 선택

### B. 공동주택/건물 메타데이터 계층

역할:

- 공동주택 단지 식별
- 준공연도, 세대수, 기본 구조 정보 확보
- 건물 개요와 인허가/주택 인허가 정보 연결

필요 어댑터:

- `KaptComplexListAdapter`
- `KaptComplexBasicAdapter`
- `BuildingHubPermitAdapter`
- `HousingHubPermitAdapter`
- `VworldBuildingAdapter`

사용 목적:

- 단지명/주소로 프로젝트 기본 카드를 자동 구성
- 평형 후보, 건물 개요, 층수, 사용승인일, 세대수, 전유/공용 관련 속성 확보

### C. 도면 계층

역할:

- 도면 목록 조회
- 도면 이미지/파일 식별
- 출처/라이선스 관리

필요 어댑터:

- `OfficialFloorPlanAdapter` // 건축물 생애이력 관리시스템 승인형
- `LicensedPlanProviderAdapter` // 향후 민간 라이선스 연동
- `UserUploadPlanAdapter`

### D. 내부 도면 정규화 계층

역할:

- 외부 도면 원본을 내부 표준 포맷으로 통일
- 공간/벽/문/창/발코니/욕실/주방 세그먼트화
- 신뢰도 계산

필요 서비스:

- `PlanNormalizer`
- `PlanConfidenceScorer`
- `ApproximatePlanGenerator`
- `PlanCandidateRanker`

---

## 3. 도면 확보 엔진 상세 설계

## 3-1. 핵심 서비스명

`FloorPlanResolutionService`

### 입력

- 표준 주소
- 단지명
- 동/호
- 건축물대장키(있으면)
- 전용면적 / 공급면적 후보
- 방 개수 / 욕실 개수(사용자 보정 입력 가능)

### 출력

- 도면 후보 리스트
- 후보별 신뢰도
- 원본 출처
- 라이선스 상태
- 정규화 결과
- 사용자 선택 필요 여부

---

## 3-2. 파이프라인

### Step 1. 주소 정규화

입력 예:

- “잠실 리센츠 201동 1203호”
- “서울 송파구 올림픽로 135”
- “헬리오시티 101-702”

처리:

- 주소 파싱
- 표준 도로명/지번 주소 변환
- 좌표 확보
- 동/층/호 분해
- 아파트명/단지명 후보 추출

결과:

- `normalized_address`
- `road_code`
- `building_main_no`
- `building_sub_no`
- `legal_dong_code`
- `lat/lng`

### Step 2. 공동주택 여부 판별

로직:

- K-apt 단지 목록/기본정보 조회
- 건축/주택 인허가 정보 조회
- 공동주택/아파트/주상복합/오피스텔/단독주택 구분

### Step 3. 도면 소스 순차 조회

우선순위:

1. 내부 캐시
2. 공식 승인 도면 API
3. 라이선스 민간 소스
4. 같은 단지/같은 평형 유사 도면
5. 근사 평면 생성

### Step 4. 정규화

원본 도면을 내부 공통 구조로 변환:

- space list
- wall graph
- opening graph
- wet zone map
- dimension candidates
- uncertainty map

### Step 5. 신뢰도 계산

점수 구성 예시:

- source reliability: 0~40
- address exactness: 0~15
- complex match: 0~15
- building match: 0~10
- unit/line match: 0~10
- area match: 0~5
- user confirmation bonus: 0~5

등급:

- 90~100: EXACT
- 75~89: HIGH
- 55~74: APPROX
- 0~54: LOW

### Step 6. 사용자 확인 UX

- EXACT: 바로 프로젝트 도면으로 채택
- HIGH: 추천 후보 1개 + 대체 후보 2개 제안
- APPROX: “이 구조가 가장 비슷한가요?” 선택
- LOW: 사용자 입력 추가 유도 + 업로드 fallback

---

## 3-3. 도면 정규화 내부 포맷

```json
{
  "planId": "uuid",
  "sourceType": "OFFICIAL|LICENSED|USER_UPLOAD|APPROX",
  "confidenceGrade": "EXACT",
  "area": {
    "exclusiveM2": 84.97,
    "supplyM2": 112.3
  },
  "spaces": [
    {
      "spaceId": "uuid",
      "type": "LIVING_ROOM",
      "name": "거실",
      "polygon": [
        [0, 0],
        [10, 0],
        [10, 8],
        [0, 8]
      ],
      "areaM2": 18.5,
      "isWetZone": false
    }
  ],
  "openings": [
    {
      "openingId": "uuid",
      "type": "WINDOW",
      "spaceId": "uuid",
      "position": { "x": 9.5, "y": 2.0 },
      "widthMm": 2400
    }
  ],
  "walls": [
    {
      "wallId": "uuid",
      "kind": "UNKNOWN",
      "structuralProbability": 0.64,
      "start": { "x": 0, "y": 0 },
      "end": { "x": 10, "y": 0 }
    }
  ],
  "measurements": {
    "requiredManualChecks": [
      "거실 폭 실측",
      "주방-다이닝 간 벽체 두께 확인",
      "욕실 젖은 구역 배수구 위치 확인"
    ]
  }
}
```

---

## 4. 제품 정보구조(IA)

## 4-1. 1차 웹 라우트

### 공개 라우트

- `/`
- `/search`
- `/guide`
- `/guide/{category}`
- `/guide/{category}/{slug}`
- `/experts`
- `/experts/{expertId}`
- `/pricing`
- `/about`

### 인증 필요 라우트

- `/projects`
- `/projects/{projectId}`
- `/projects/{projectId}/home`
- `/projects/{projectId}/property`
- `/projects/{projectId}/plan`
- `/projects/{projectId}/style`
- `/projects/{projectId}/process`
- `/projects/{projectId}/process/{stepKey}`
- `/projects/{projectId}/qa`
- `/projects/{projectId}/qa/{questionId}`
- `/projects/{projectId}/experts`
- `/projects/{projectId}/settings`

### 관리자 라우트

- `/admin`
- `/admin/projects`
- `/admin/knowledge`
- `/admin/process-catalog`
- `/admin/experts`
- `/admin/leads`
- `/admin/plan-sources`
- `/admin/integration-logs`

---

## 5. 화면설계 상세

## 5-1. 홈(`/`)

### 목적

- 최대 이탈 없이 주소 입력 시작
- 서비스 가치 제안 즉시 전달
- 비회원 상태에서도 첫 결과를 보여줌

### 상단 영역

- 헤드라인: “주소만 입력하면 우리 집 인테리어 계획을 시작할 수 있어요”
- 서브카피: “도면 자동 찾기, 공정 순서, 사진 질문, 전문가 연결까지”
- 1차 CTA: 주소/아파트명 검색 입력창
- 2차 CTA: 도면 업로드(서브)

### 중단 영역

- “내가 지금 하려는 일” 카드
  - 리모델링 준비
  - 부분 시공 찾기
  - 공사 중 문제 해결
  - 견적 검토

### 하단 영역

- 인기 질문
- 인기 공정
- 지역/아파트 랜딩
- 리뷰/사례

### 주요 이벤트

- `home_search_submitted`
- `home_intent_selected`
- `home_plan_upload_clicked`

---

## 5-2. 주소 검색 결과(`/search`)

### 목적

- 사용자가 입력한 집을 정확히 확정
- 공동주택/동·호/평형 후보 자동화

### 좌측

- 검색 결과 리스트
- 단지명, 주소, 준공연도, 세대수, 건물유형

### 우측

- 선택된 집 상세 카드
- 동/호 선택 UI
- 평형 후보
- 도면 확보 가능성 배지
  - 확보 가능 높음
  - 일부 후보 있음
  - 근사 구조 필요

### 액션

- “이 집으로 프로젝트 만들기”
- “동/호를 모르겠어요”
- “정확한 도면이 다르면 나중에 수정할게요”

---

## 5-3. 프로젝트 생성 모달

### 입력

- 프로젝트 이름
- 입주 전/거주 중
- 전체/부분/점검
- 예산 범위
- 유지할 요소
- 셀프로 하고 싶은 범위

### 저장 결과

- `project`
- `property`
- `project_scope`

---

## 5-4. 프로젝트 홈(`/projects/{id}/home`)

### 목적

- 사용자가 “지금 뭘 해야 하는지” 한 화면에서 이해

### 카드 1. 우리 집 요약

- 단지/주소
- 준공연도
- 평형
- 도면 신뢰도
- 구조 요약

### 카드 2. 지금 해야 할 일

예:

- 도면 후보 확인
- 전기 공정 체크리스트 검토
- 욕실 사진 업로드 후 방수 상태 질문

### 카드 3. 다음 공정 전에 결정할 것

예:

- 마루 종류
- 콘센트 추가 위치
- 욕실 타일 방식

### 카드 4. 추천 전문가

현재 단계와 질문 이력을 반영해 2~4명 노출

### 카드 5. 최근 질문

- 사진 썸네일
- 질문 제목
- 답변 상태

---

## 5-5. 집 정보/도면 페이지(`/projects/{id}/property`, `/plan`)

### 탭

- 집 정보
- 주소/동·호
- 평형/면적
- 단지 기본정보
- 도면 후보
- 정규화 구조
- 실측 체크포인트

### 도면 화면 구성

좌측:

- 도면 후보 리스트
- 신뢰도
- 출처
- 라이선스/검증 상태

중앙:

- 도면 뷰어(확대/축소)
- 공간 하이라이트

우측:

- 구조 요약
- 방/욕실/주방/발코니 추정
- 실측 필요 사항
- 위험 추정
  - 구조벽 판단 불가
  - 누수/결로 추정 불가
  - 실측 필요

### 액션

- “이 도면 사용”
- “가장 비슷한 구조로 수정”
- “도면 업로드로 보정”
- “평형 다시 선택”

---

## 5-6. 스타일 페이지(`/projects/{id}/style`)

### 목적

- 영감 + 실행 계획 연결

### 상단 입력

- 스타일 선택
- 예산 슬라이더
- 유지할 요소
- 공간 선택
- 톤/무드

### 본문

- 이미지 그리드
- 각 이미지 카드에:
  - 스타일명
  - 난이도
  - 예산 영향
  - 필요 공정
  - 저장 버튼
  - 비교 버튼

### 하단

- 선택한 스타일로 계획 생성
- 관련 전문가 보기

---

## 5-7. 공정 플래너(`/projects/{id}/process`)

### 목적

- 반셀프 핵심 가치 제공

### 상단

- 전체 진행률
- 현재 단계 배지
- 일정 개요
- 위험 알림

### 메인

- 타임라인 방식 단계 목록
  - 철거
  - 설비
  - 전기
  - 목공
  - 타일/방수
  - 도배
  - 마루
  - 필름
  - 조명
  - 가구
  - 마감/청소

각 단계 카드:

- 상태(예정/진행중/검토필요/완료)
- 꼭 결정할 것
- 셀프 가능 범위
- 전문가 필요 범위
- 예상 기간
- 질문하기 버튼
- 전문가 보기 버튼

---

## 5-8. 공정 상세 페이지(`/projects/{id}/process/{stepKey}`)

### 정보 블록

1. 이 공정의 목적
2. 시작 전 체크리스트
3. 이 단계에서 정해야 할 것
4. 셀프로 가능한 범위
5. 전문가 필수 범위
6. 자주 하는 실수
7. 다음 공정 전에 확인할 것
8. FAQ
9. 관련 사진 질문
10. 연결 전문가

### 예시: `electrical`

- 콘센트 위치
- 회로 분리
- 에어컨 전용선
- 주방 가전 전력 분리
- 조명 스위치
- 인터넷/공유기 위치
- 전기기사 필수 구간

---

## 5-9. 사진 질문 페이지(`/projects/{id}/qa`)

### 입력

- 사진 업로드(복수)
- 질문 텍스트
- 공간 선택
- 현재 단계 자동 선택(수정 가능)

### 답변 출력 구조

1. 사진에서 보이는 상태
2. 가능한 원인
3. 지금 추가 확인할 것
4. 다음 공정 진행 가능 여부
5. 전문가 점검 필요 여부
6. 관련 체크리스트/가이드 링크

### 위험 분류

- LOW: 일반 가이드
- MEDIUM: 주의 필요
- HIGH: 전문가 점검 권장
- CRITICAL: 즉시 전문가/안전 확인

---

## 5-10. 전문가 추천 페이지(`/projects/{id}/experts`)

### 필터

- 공정
- 지역
- 일정
- 예산
- 부분시공 가능 여부
- 반셀프 협업 가능 여부

### 카드 항목

- 프로필 사진
- 업체명
- 전문 카테고리
- 지역
- 응답속도
- 후기 점수
- 최소 견적
- 부분시공 가능 여부
- 포트폴리오
- 문의 버튼

### CTA

- 프로젝트 정보 자동 첨부
- 최근 질문 사진 자동 첨부(사용자 동의 시)
- 희망 일정/예산 자동 반영

---

## 5-11. 관리자 화면

### 관리자 메뉴

- 프로젝트 조회
- 도면 후보 검수
- 질문/답변 로그 검수
- 공정 가이드 편집
- 전문가 승인/정지
- 리드 추적
- API 실패 로그 조회
- 도면 소스 provenance 검수

---

## 6. 사용자 플로우 상세

## 플로우 A. 주소 기반 신규 사용자

1. 홈에서 주소 입력
2. 검색 결과에서 집 확정
3. 프로젝트 범위 설정
4. 도면 자동 탐색
5. 후보 선택 또는 근사 평면 확인
6. 첫 스타일 이미지 1~3장 생성
7. 공정 플래너 초안 생성
8. 프로젝트 저장 유도(회원가입)

## 플로우 B. 문제 해결형 사용자

1. 홈에서 “공사 중 문제 해결” 선택
2. 주소 입력 또는 빠른 프로젝트 생성
3. 현재 공정 선택
4. 사진 업로드
5. 질문 제출
6. 답변 확인
7. 관련 공정 체크리스트 열람
8. 전문가 문의

## 플로우 C. 부분 시공 문의형 사용자

1. 부분시공 선택
2. 주소 입력
3. 문제 공간 선택
4. 사진/설명 입력
5. 전문가 추천
6. 리드 생성
7. 상담/견적 연결

---

## 7. 시스템 아키텍처

## 7-1. 권장 구조

- 프론트: Next.js App Router + TypeScript + Tailwind
- 백엔드: Spring Boot + PostgreSQL + Redis + S3
- 검색/임베딩: pgvector 또는 별도 벡터 저장소
- 비동기 처리: Redis queue 또는 메시지 큐
- AI 오케스트레이션: 백엔드 내부 서비스 모듈

## 7-2. 백엔드 모듈

- `auth`
- `project`
- `property`
- `address-resolution`
- `floor-plan`
- `style-preview`
- `process-guide`
- `visual-qa`
- `expert-marketplace`
- `billing`
- `admin`
- `integration`
- `common`

## 7-3. 외부 연동 어댑터

- `KakaoLocalClient`
- `JusoAddressClient`
- `KaptClient`
- `BuildingHubClient`
- `HousingHubClient`
- `VworldClient`
- `OfficialPlanClient`
- `LicensedPlanClient`
- `AiImageClient`
- `VisionQaClient`
- `EmbeddingClient`

---

## 8. 데이터베이스 설계

아래는 **PostgreSQL 기준 권장 스키마**다.  
PK는 기본적으로 `uuid` 사용.

## 8-1. 공통 규칙

- 시간 컬럼: `created_at`, `updated_at`
- 삭제 전략: 핵심 테이블은 soft delete(`deleted_at`) 고려
- JSONB 사용 대상:
  - 외부 API 원본 payload
  - 정규화 도면 구조
  - AI 입력/출력 메타
- 인덱스:
  - 주소검색, 프로젝트 조회, 전문가 조회, 질문 검색 중심 설계

---

## 8-2. 사용자/권한 도메인

### `users`

- `id` uuid pk
- `email` varchar(255) unique not null
- `password_hash` varchar(255) null
- `provider` varchar(30) not null default 'email'
- `name` varchar(100) not null
- `phone` varchar(30) null
- `role` varchar(30) not null default 'USER'
- `marketing_consent` boolean not null default false
- `last_login_at` timestamptz null
- `created_at` timestamptz not null
- `updated_at` timestamptz not null

인덱스:

- `ux_users_email`
- `ix_users_role`

### `project_members`

- `id` uuid pk
- `project_id` uuid fk -> projects.id
- `user_id` uuid fk -> users.id
- `member_role` varchar(30) not null // OWNER, EDITOR, VIEWER
- `created_at`
- `updated_at`

유니크:

- `(project_id, user_id)`

---

## 8-3. 프로젝트/부동산 도메인

### `projects`

- `id` uuid pk
- `owner_user_id` uuid fk -> users.id
- `title` varchar(150) not null
- `status` varchar(30) not null // DRAFT, ACTIVE, PAUSED, CLOSED
- `project_type` varchar(30) not null // FULL, PARTIAL, ISSUE_CHECK
- `living_status` varchar(30) not null // BEFORE_MOVE_IN, OCCUPIED
- `budget_min` integer null
- `budget_max` integer null
- `currency` varchar(10) default 'KRW'
- `current_process_step` varchar(50) null
- `onboarding_completed` boolean not null default false
- `created_at`
- `updated_at`

인덱스:

- `ix_projects_owner_user_id`
- `ix_projects_status`
- `ix_projects_project_type`

### `properties`

- `id` uuid pk
- `project_id` uuid fk -> projects.id unique
- `property_type` varchar(30) not null // APARTMENT, OFFICETEL, VILLA, HOUSE
- `country_code` varchar(10) not null default 'KR'
- `sido` varchar(50) null
- `sigungu` varchar(50) null
- `eup_myeon_dong` varchar(80) null
- `road_address` varchar(255) null
- `jibun_address` varchar(255) null
- `detail_address` varchar(100) null
- `apartment_name` varchar(150) null
- `building_no` varchar(50) null
- `dong_no` varchar(50) null
- `ho_no` varchar(50) null
- `postal_code` varchar(20) null
- `lat` numeric(10,7) null
- `lng` numeric(10,7) null
- `completion_year` integer null
- `approval_date` date null
- `household_count` integer null
- `supply_area_m2` numeric(10,2) null
- `exclusive_area_m2` numeric(10,2) null
- `room_count` integer null
- `bathroom_count` integer null
- `balcony_count` integer null
- `heating_type` varchar(50) null
- `raw_summary` jsonb null
- `created_at`
- `updated_at`

인덱스:

- `ix_properties_project_id`
- `ix_properties_apartment_name`
- `ix_properties_completion_year`
- `ix_properties_lat_lng`

### `address_resolution_logs`

- `id` uuid pk
- `project_id` uuid fk -> projects.id
- `input_query` varchar(255) not null
- `normalized_road_address` varchar(255) null
- `normalized_jibun_address` varchar(255) null
- `road_code` varchar(30) null
- `building_main_no` varchar(20) null
- `building_sub_no` varchar(20) null
- `legal_dong_code` varchar(20) null
- `source` varchar(50) not null
- `confidence_score` numeric(5,2) not null
- `raw_payload` jsonb not null
- `created_at`

인덱스:

- `ix_address_resolution_logs_project_id`
- `ix_address_resolution_logs_road_code`

### `external_property_refs`

- `id` uuid pk
- `property_id` uuid fk -> properties.id
- `provider` varchar(50) not null // KAPT, BLDHUB, HOUSEHUB, VWORLD, OFFICIAL_PLAN
- `external_key` varchar(255) not null
- `ref_type` varchar(50) not null // COMPLEX_CODE, BLD_KEY, PERMIT_PK
- `metadata` jsonb null
- `created_at`
- `updated_at`

유니크:

- `(provider, external_key, ref_type)`

---

## 8-4. 도면 도메인

### `floor_plan_sources`

- `id` uuid pk
- `provider` varchar(50) not null // OFFICIAL, LICENSED, USER_UPLOAD, GENERATED
- `license_status` varchar(30) not null // APPROVED, RESTRICTED, USER_OWNED, INTERNAL
- `access_scope` varchar(30) not null // ORG_ONLY, COMMERCIAL_ALLOWED, INTERNAL_ONLY
- `provider_doc_ref` varchar(255) null
- `created_at`
- `updated_at`

### `floor_plan_candidates`

- `id` uuid pk
- `project_id` uuid fk -> projects.id
- `property_id` uuid fk -> properties.id
- `floor_plan_source_id` uuid fk -> floor_plan_sources.id
- `provider_plan_key` varchar(255) null
- `source_type` varchar(30) not null
- `match_type` varchar(30) not null // EXACT, SAME_COMPLEX_SIMILAR_AREA, APPROX_GENERATED
- `confidence_score` numeric(5,2) not null
- `confidence_grade` varchar(20) not null
- `exclusive_area_m2` numeric(10,2) null
- `supply_area_m2` numeric(10,2) null
- `room_count` integer null
- `bathroom_count` integer null
- `layout_label` varchar(100) null
- `is_selected` boolean not null default false
- `selection_reason` varchar(255) null
- `raw_payload` jsonb null
- `created_at`
- `updated_at`

인덱스:

- `ix_floor_plan_candidates_project_id`
- `ix_floor_plan_candidates_property_id`
- `ix_floor_plan_candidates_confidence_score`
- `ix_floor_plan_candidates_is_selected`

### `floor_plan_files`

- `id` uuid pk
- `floor_plan_candidate_id` uuid fk -> floor_plan_candidates.id
- `file_type` varchar(20) not null // IMAGE, PDF, JSON
- `storage_key` varchar(255) not null
- `original_filename` varchar(255) null
- `mime_type` varchar(100) not null
- `width_px` integer null
- `height_px` integer null
- `page_count` integer null
- `checksum` varchar(128) null
- `created_at`
- `updated_at`

### `normalized_floor_plans`

- `id` uuid pk
- `floor_plan_candidate_id` uuid fk -> floor_plan_candidates.id unique
- `normalization_status` varchar(30) not null // PENDING, READY, FAILED
- `plan_json` jsonb not null
- `uncertainty_json` jsonb null
- `manual_check_items` jsonb null
- `normalized_by` varchar(30) not null // MODEL, RULE_ENGINE, HYBRID
- `created_at`
- `updated_at`

GIN index:

- `gin_normalized_floor_plans_plan_json`

### `normalized_plan_spaces`

- `id` uuid pk
- `normalized_floor_plan_id` uuid fk -> normalized_floor_plans.id
- `space_key` varchar(50) not null
- `space_name` varchar(100) not null
- `space_type` varchar(50) not null
- `polygon_json` jsonb not null
- `area_m2` numeric(10,2) null
- `is_wet_zone` boolean not null default false
- `created_at`
- `updated_at`

인덱스:

- `ix_normalized_plan_spaces_plan_id`
- `ix_normalized_plan_spaces_space_type`

### `normalized_plan_openings`

- `id` uuid pk
- `normalized_floor_plan_id` uuid fk -> normalized_floor_plans.id
- `space_id` uuid fk -> normalized_plan_spaces.id null
- `opening_type` varchar(30) not null // DOOR, WINDOW, SLIDING_DOOR
- `position_json` jsonb not null
- `width_mm` integer null
- `metadata` jsonb null
- `created_at`
- `updated_at`

### `normalized_plan_walls`

- `id` uuid pk
- `normalized_floor_plan_id` uuid fk -> normalized_floor_plans.id
- `wall_kind` varchar(30) not null // UNKNOWN, INTERIOR, EXTERIOR, STRUCTURAL_LIKELY
- `structural_probability` numeric(5,2) null
- `line_json` jsonb not null
- `thickness_mm` integer null
- `created_at`
- `updated_at`

### `plan_selection_events`

- `id` uuid pk
- `project_id` uuid fk -> projects.id
- `floor_plan_candidate_id` uuid fk -> floor_plan_candidates.id
- `action` varchar(30) not null // AUTO_SELECTED, USER_SELECTED, USER_REJECTED
- `reason` varchar(255) null
- `created_by` uuid fk -> users.id null
- `created_at`

---

## 8-5. 프로젝트 범위/스타일 도메인

### `project_scopes`

- `id` uuid pk
- `project_id` uuid fk -> projects.id unique
- `scope_type` varchar(30) not null // FULL, PARTIAL, ISSUE_CHECK
- `spaces_targeted` jsonb null
- `keep_items` jsonb null
- `self_work_items` jsonb null
- `desired_style_keywords` jsonb null
- `schedule_start_target` date null
- `schedule_end_target` date null
- `special_notes` text null
- `created_at`
- `updated_at`

### `style_presets`

- `id` uuid pk
- `key` varchar(50) unique not null
- `name` varchar(100) not null
- `description` text null
- `prompt_template` text not null
- `active` boolean not null default true
- `created_at`
- `updated_at`

### `project_style_selections`

- `id` uuid pk
- `project_id` uuid fk -> projects.id
- `style_preset_id` uuid fk -> style_presets.id
- `space_type` varchar(50) not null
- `priority` integer not null default 1
- `selected` boolean not null default false
- `created_at`
- `updated_at`

### `generated_style_images`

- `id` uuid pk
- `project_id` uuid fk -> projects.id
- `style_preset_id` uuid fk -> style_presets.id null
- `space_type` varchar(50) not null
- `prompt_text` text not null
- `negative_prompt_text` text null
- `generation_status` varchar(30) not null // PENDING, SUCCESS, FAILED
- `storage_key` varchar(255) null
- `thumbnail_key` varchar(255) null
- `seed` varchar(100) null
- `model_name` varchar(100) null
- `metadata` jsonb null
- `liked` boolean not null default false
- `created_at`
- `updated_at`

---

## 8-6. 공정/가이드 도메인

### `process_catalogs`

- `id` uuid pk
- `key` varchar(50) unique not null
- `name` varchar(100) not null
- `description` text null
- `default_order` integer not null
- `active` boolean not null default true
- `created_at`
- `updated_at`

### `process_guides`

- `id` uuid pk
- `process_catalog_id` uuid fk -> process_catalogs.id
- `title` varchar(150) not null
- `summary` text not null
- `purpose_text` text not null
- `start_conditions_text` text not null
- `decision_points_text` text not null
- `self_service_scope_text` text not null
- `pro_required_scope_text` text not null
- `common_mistakes_text` text not null
- `next_step_check_text` text not null
- `safety_level` varchar(20) not null // LOW, MEDIUM, HIGH
- `version` integer not null default 1
- `published` boolean not null default true
- `created_at`
- `updated_at`

### `process_checklists`

- `id` uuid pk
- `process_guide_id` uuid fk -> process_guides.id
- `section_type` varchar(30) not null // BEFORE_START, DECISION, BEFORE_NEXT
- `item_order` integer not null
- `label` varchar(255) not null
- `description` text null
- `required` boolean not null default true
- `created_at`
- `updated_at`

### `project_process_plans`

- `id` uuid pk
- `project_id` uuid fk -> projects.id unique
- `status` varchar(30) not null // DRAFT, ACTIVE
- `generated_from_plan_id` uuid fk -> normalized_floor_plans.id null
- `created_at`
- `updated_at`

### `project_process_steps`

- `id` uuid pk
- `project_process_plan_id` uuid fk -> project_process_plans.id
- `process_catalog_id` uuid fk -> process_catalogs.id
- `step_order` integer not null
- `status` varchar(30) not null // TODO, READY, IN_PROGRESS, BLOCKED, DONE
- `start_target_date` date null
- `end_target_date` date null
- `is_required` boolean not null default true
- `assigned_to` varchar(30) not null default 'USER' // USER, EXPERT, MIXED
- `notes` text null
- `created_at`
- `updated_at`

유니크:

- `(project_process_plan_id, step_order)`

### `project_process_tasks`

- `id` uuid pk
- `project_process_step_id` uuid fk -> project_process_steps.id
- `task_type` varchar(30) not null // CHECK, DECISION, BOOKING, PURCHASE
- `title` varchar(255) not null
- `description` text null
- `status` varchar(30) not null // TODO, DONE
- `due_date` date null
- `required` boolean not null default true
- `created_at`
- `updated_at`

---

## 8-7. 사진 질문/답변 도메인

### `visual_questions`

- `id` uuid pk
- `project_id` uuid fk -> projects.id
- `process_step_key` varchar(50) null
- `space_type` varchar(50) null
- `question_text` text not null
- `risk_level` varchar(20) not null default 'LOW'
- `status` varchar(30) not null // SUBMITTED, ANSWERED, ESCALATED
- `asked_by_user_id` uuid fk -> users.id
- `created_at`
- `updated_at`

### `visual_question_images`

- `id` uuid pk
- `visual_question_id` uuid fk -> visual_questions.id
- `storage_key` varchar(255) not null
- `mime_type` varchar(100) not null
- `width_px` integer null
- `height_px` integer null
- `metadata` jsonb null
- `created_at`

### `visual_answers`

- `id` uuid pk
- `visual_question_id` uuid fk -> visual_questions.id unique
- `observed_text` text not null
- `possible_causes_text` text not null
- `next_checks_text` text not null
- `proceed_recommendation_text` text not null
- `expert_required` boolean not null default false
- `confidence_score` numeric(5,2) not null
- `model_name` varchar(100) null
- `source_docs` jsonb null
- `raw_output` jsonb null
- `created_at`
- `updated_at`

---

## 8-8. 전문가/리드 도메인

### `expert_categories`

- `id` uuid pk
- `key` varchar(50) unique not null
- `name` varchar(100) not null
- `active` boolean not null default true
- `created_at`
- `updated_at`

### `experts`

- `id` uuid pk
- `company_name` varchar(150) not null
- `contact_name` varchar(100) not null
- `phone` varchar(30) null
- `email` varchar(255) null
- `business_no` varchar(30) null
- `license_info` jsonb null
- `intro_text` text null
- `min_budget` integer null
- `max_budget` integer null
- `partial_work_supported` boolean not null default true
- `semi_self_collaboration_supported` boolean not null default true
- `response_score` numeric(5,2) null
- `review_score` numeric(5,2) null
- `status` varchar(30) not null // PENDING, ACTIVE, SUSPENDED
- `created_at`
- `updated_at`

### `expert_category_links`

- `id` uuid pk
- `expert_id` uuid fk -> experts.id
- `expert_category_id` uuid fk -> expert_categories.id

유니크:

- `(expert_id, expert_category_id)`

### `expert_service_regions`

- `id` uuid pk
- `expert_id` uuid fk -> experts.id
- `sido` varchar(50) not null
- `sigungu` varchar(50) null
- `created_at`

### `expert_portfolios`

- `id` uuid pk
- `expert_id` uuid fk -> experts.id
- `title` varchar(150) not null
- `description` text null
- `storage_key` varchar(255) null
- `metadata` jsonb null
- `created_at`
- `updated_at`

### `expert_leads`

- `id` uuid pk
- `project_id` uuid fk -> projects.id
- `expert_id` uuid fk -> experts.id
- `requested_category_id` uuid fk -> expert_categories.id
- `lead_status` varchar(30) not null // NEW, CONTACTED, QUOTED, WON, LOST
- `budget_min` integer null
- `budget_max` integer null
- `desired_start_date` date null
- `message` text null
- `attachment_payload` jsonb null
- `created_by_user_id` uuid fk -> users.id
- `created_at`
- `updated_at`

### `expert_lead_events`

- `id` uuid pk
- `expert_lead_id` uuid fk -> expert_leads.id
- `event_type` varchar(30) not null
- `payload` jsonb null
- `created_at`

---

## 8-9. 콘텐츠/운영 도메인

### `knowledge_articles`

- `id` uuid pk
- `slug` varchar(150) unique not null
- `category_key` varchar(50) not null
- `title` varchar(200) not null
- `summary` text null
- `body_md` text not null
- `published` boolean not null default true
- `seo_title` varchar(200) null
- `seo_description` varchar(255) null
- `created_at`
- `updated_at`

### `faq_entries`

- `id` uuid pk
- `category_key` varchar(50) not null
- `question` varchar(255) not null
- `answer_md` text not null
- `published` boolean not null default true
- `created_at`
- `updated_at`

### `integration_call_logs`

- `id` uuid pk
- `provider` varchar(50) not null
- `operation` varchar(100) not null
- `request_id` varchar(100) null
- `status_code` varchar(30) null
- `success` boolean not null
- `latency_ms` integer null
- `error_message` text null
- `request_meta` jsonb null
- `response_meta` jsonb null
- `created_at`

### `ai_job_runs`

- `id` uuid pk
- `job_type` varchar(50) not null // PLAN_NORMALIZE, STYLE_GEN, VISUAL_QA
- `entity_type` varchar(50) not null
- `entity_id` uuid not null
- `status` varchar(30) not null
- `model_name` varchar(100) null
- `input_tokens` integer null
- `output_tokens` integer null
- `cost_estimate` numeric(10,4) null
- `payload` jsonb null
- `created_at`
- `updated_at`

### `audit_logs`

- `id` uuid pk
- `actor_user_id` uuid null
- `entity_type` varchar(50) not null
- `entity_id` uuid null
- `action` varchar(50) not null
- `payload` jsonb null
- `created_at`

---

## 9. 핵심 API 명세

응답 공통 포맷:

```json
{
  "success": true,
  "data": {},
  "meta": {
    "requestId": "uuid",
    "timestamp": "2026-03-26T12:00:00Z"
  },
  "error": null
}
```

에러 포맷:

```json
{
  "success": false,
  "data": null,
  "meta": {
    "requestId": "uuid",
    "timestamp": "2026-03-26T12:00:00Z"
  },
  "error": {
    "code": "PROPERTY_NOT_FOUND",
    "message": "입력한 주소로 주택을 찾지 못했습니다.",
    "details": {}
  }
}
```

---

## 9-1. 주소/집 식별 API

### `POST /api/v1/address/search`

목적:

- 사용자가 입력한 문자열로 주소/아파트 후보 검색

요청:

```json
{
  "query": "잠실 리센츠 201동 1203호"
}
```

응답:

```json
{
  "success": true,
  "data": {
    "candidates": [
      {
        "displayName": "리센츠",
        "roadAddress": "서울특별시 송파구 올림픽로 135",
        "jibunAddress": "서울특별시 송파구 잠실동 ...",
        "propertyType": "APARTMENT",
        "lat": 37.0,
        "lng": 127.0,
        "dongCandidates": ["201", "202"],
        "hoCandidateRequired": true,
        "complexHint": {
          "completionYear": 2008,
          "householdCount": 5563
        }
      }
    ]
  },
  "meta": {},
  "error": null
}
```

### `POST /api/v1/address/detail-options`

목적:

- 선택된 주소 기준 동/층/호 옵션 확장

요청:

```json
{
  "roadCode": "117103123456",
  "buildingMainNo": "135",
  "buildingSubNo": "0",
  "queryType": "dong"
}
```

### `POST /api/v1/property/resolve`

목적:

- 주소 확정 후 공동주택 메타데이터 취합

요청:

```json
{
  "roadAddress": "서울특별시 송파구 올림픽로 135",
  "dongNo": "201",
  "hoNo": "1203"
}
```

응답:

```json
{
  "success": true,
  "data": {
    "propertySummary": {
      "propertyType": "APARTMENT",
      "apartmentName": "리센츠",
      "completionYear": 2008,
      "householdCount": 5563,
      "exclusiveAreaCandidates": [59.97, 84.99],
      "roomCountCandidates": [3],
      "bathroomCountCandidates": [2]
    },
    "externalRefs": [
      {
        "provider": "KAPT",
        "refType": "COMPLEX_CODE",
        "externalKey": "A10027890"
      }
    ]
  },
  "meta": {},
  "error": null
}
```

---

## 9-2. 프로젝트 API

### `POST /api/v1/projects`

요청:

```json
{
  "title": "잠실 리센츠 인테리어",
  "projectType": "FULL",
  "livingStatus": "BEFORE_MOVE_IN",
  "budgetMin": 30000000,
  "budgetMax": 60000000
}
```

### `GET /api/v1/projects`

### `GET /api/v1/projects/{projectId}`

### `PATCH /api/v1/projects/{projectId}`

---

## 9-3. 속성/도면 API

### `POST /api/v1/projects/{projectId}/property`

목적:

- 프로젝트에 확정된 집 정보 연결

요청:

```json
{
  "roadAddress": "서울특별시 송파구 올림픽로 135",
  "jibunAddress": "서울특별시 송파구 잠실동 ...",
  "apartmentName": "리센츠",
  "dongNo": "201",
  "hoNo": "1203",
  "exclusiveAreaM2": 84.99
}
```

### `POST /api/v1/projects/{projectId}/floor-plans/resolve`

목적:

- 도면 후보 수집 시작

응답:

```json
{
  "success": true,
  "data": {
    "resolutionStatus": "PROCESSING",
    "jobId": "uuid"
  }
}
```

### `GET /api/v1/projects/{projectId}/floor-plans`

응답:

```json
{
  "success": true,
  "data": {
    "selectedPlanId": "uuid",
    "candidates": [
      {
        "id": "uuid",
        "sourceType": "OFFICIAL",
        "matchType": "EXACT",
        "confidenceScore": 93.2,
        "confidenceGrade": "EXACT",
        "layoutLabel": "84A",
        "exclusiveAreaM2": 84.99,
        "thumbnailUrl": "signed-url",
        "licenseStatus": "APPROVED",
        "manualCheckItems": ["거실 폭 실측"]
      }
    ]
  }
}
```

### `POST /api/v1/projects/{projectId}/floor-plans/{candidateId}/select`

요청:

```json
{
  "reason": "MOST_SIMILAR"
}
```

### `POST /api/v1/projects/{projectId}/floor-plans/upload`

multipart:

- file
- planType
- memo

### `GET /api/v1/projects/{projectId}/floor-plans/{candidateId}/normalized`

정규화 JSON 반환

---

## 9-4. 스타일 API

### `POST /api/v1/projects/{projectId}/styles/generate`

요청:

```json
{
  "spaceTypes": ["LIVING_ROOM", "KITCHEN"],
  "stylePresetKey": "WHITE_MINIMAL",
  "budgetLevel": "MID",
  "keepItems": ["WINDOWS", "KITCHEN_CABINET"],
  "extraPrompt": "신혼집 느낌, 과한 럭셔리 금지"
}
```

### `GET /api/v1/projects/{projectId}/styles/images`

### `POST /api/v1/projects/{projectId}/styles/images/{imageId}/like`

---

## 9-5. 공정 플래너 API

### `POST /api/v1/projects/{projectId}/process-plan/generate`

입력:

- 현재 도면
- 프로젝트 범위
- 유지할 요소
- 거주 여부

응답:

- 단계 리스트
- 기본 일정
- 필수 단계/생략 단계

### `GET /api/v1/projects/{projectId}/process-plan`

### `GET /api/v1/projects/{projectId}/process-plan/steps/{stepKey}`

### `PATCH /api/v1/projects/{projectId}/process-plan/steps/{stepId}`

### `PATCH /api/v1/projects/{projectId}/process-plan/tasks/{taskId}`

---

## 9-6. 사진 질문 API

### `POST /api/v1/projects/{projectId}/visual-questions`

multipart + json:

- files[]
- questionText
- processStepKey
- spaceType

응답:

```json
{
  "success": true,
  "data": {
    "questionId": "uuid",
    "status": "PROCESSING"
  }
}
```

### `GET /api/v1/projects/{projectId}/visual-questions`

### `GET /api/v1/projects/{projectId}/visual-questions/{questionId}`

답변 예시:

```json
{
  "success": true,
  "data": {
    "question": {
      "id": "uuid",
      "questionText": "여기 누수야?"
    },
    "answer": {
      "riskLevel": "HIGH",
      "observedText": "천장 모서리 주변 변색과 얼룩이 보입니다.",
      "possibleCausesText": "누수 또는 결로 가능성이 있습니다.",
      "nextChecksText": "비 온 뒤 변화 여부, 상부 배관 위치를 확인하세요.",
      "proceedRecommendationText": "도배 전 누수 원인 확인을 권장합니다.",
      "expertRequired": true,
      "confidenceScore": 71.3
    },
    "relatedGuideLinks": [
      {
        "title": "누수/결로 초기 확인 가이드",
        "slug": "leak-condensation-basic"
      }
    ]
  }
}
```

---

## 9-7. 전문가 API

### `GET /api/v1/expert-categories`

### `GET /api/v1/experts`

쿼리:

- `categoryKey`
- `sido`
- `sigungu`
- `partialSupported`
- `budgetMin`
- `budgetMax`

### `GET /api/v1/experts/{expertId}`

### `POST /api/v1/projects/{projectId}/expert-leads`

요청:

```json
{
  "expertId": "uuid",
  "requestedCategoryKey": "WALLPAPER",
  "budgetMin": 3000000,
  "budgetMax": 5000000,
  "desiredStartDate": "2026-04-20",
  "message": "도배와 부분 퍼티 보수를 함께 문의합니다."
}
```

---

## 9-8. 관리자 API

- `GET /api/v1/admin/plan-candidates`
- `PATCH /api/v1/admin/plan-candidates/{id}/approve`
- `GET /api/v1/admin/experts`
- `PATCH /api/v1/admin/experts/{id}/status`
- `POST /api/v1/admin/process-guides`
- `PATCH /api/v1/admin/process-guides/{id}`
- `GET /api/v1/admin/integration-logs`

---

## 10. 비동기 작업 설계

## 10-1. 작업 큐 종류

- `ADDRESS_RESOLUTION`
- `PROPERTY_ENRICHMENT`
- `FLOOR_PLAN_RESOLUTION`
- `PLAN_NORMALIZATION`
- `STYLE_IMAGE_GENERATION`
- `VISUAL_QA_ANALYSIS`
- `PROCESS_PLAN_GENERATION`
- `LEAD_NOTIFICATION`

## 10-2. 큐 설계 규칙

- idempotency key 사용
- 외부 API timeout + retry 정책
- 장애 시 dead-letter queue
- 재시도 횟수 제한
- 사용자 화면에는 polling 또는 websocket 상태 반영

---

## 11. 핵심 비즈니스 로직

## 11-1. 프로젝트 생성 시 자동 작업

1. 프로젝트 저장
2. 주소 해결
3. 부동산 메타데이터 수집
4. 도면 해결 작업 enqueue
5. 첫 스타일 preset 제안
6. 공정 플랜 초안 생성

## 11-2. 전문가 추천 로직

입력:

- 현재 공정
- 최근 질문 위험도
- 지역
- 예산
- 부분시공 가능 여부

출력:

- 카테고리 1순위/2순위
- 추천 전문가 목록
- 예상 문의 전환 점수

## 11-3. 사진 질문 리스크 분기

- HIGH 이상이면
  - 답변 하단에 전문가 CTA 고정
  - “확정 진단 아님” 배지
  - 관련 공정 진행 경고 표시

---

## 12. 운영/관리 설계

## 12-1. 필수 관리자 기능

- 도면 후보 강제 선택/제외
- 잘못된 주소-단지 매칭 수정
- 공정 가이드 버전 관리
- 전문가 승인/정지
- 리드 품질 추적
- 위험 답변 샘플 검수

## 12-2. 지표 설계

### 유입/활성

- 주소 검색 시작률
- 주소 검색 성공률
- 프로젝트 생성률
- 첫 스타일 이미지 생성률

### 핵심 activation

- 도면 후보 선택률
- 공정 플래너 진입률
- 첫 질문 제출률
- 전문가 페이지 진입률

### 매출

- 리드 생성률
- 리드→상담 전환률
- 상담→견적 전환률
- 견적→계약 추정 전환률
- 유료 리포트 구매율

### 품질

- 주소 식별 실패율
- 도면 정확도 이슈율
- 질문 답변 재질문율
- 전문가 응답 SLA

---

## 13. 보안/법무/리스크 설계

## 13-1. 반드시 필요한 정책

- 도면 원본 출처 저장
- 라이선스 상태 저장
- 상업적 이용 가능 여부 저장
- 사용자 업로드 저작권 동의
- AI 답변 면책/고지
- 고위험 질문 가이드 제한

## 13-2. 고위험 카테고리

- 구조벽 철거 가능 여부
- 전기 안전 확정 판단
- 가스 설비 판단
- 누수 원인 확정 진단
- 방수 성능 확정 진단

이 경우:

- “가능성” 형태로만 답변
- 추가 점검 항목 안내
- 전문가 점검 권장

---

## 14. MVP 범위 확정안

## 14-1. 반드시 넣을 것

- 주소 검색/정규화
- 공동주택 메타데이터 취합
- 도면 후보 확보 + fallback
- 프로젝트 생성
- 스타일 예시 이미지
- 공정 플래너
- 사진 질문 답변
- 전문가 리드 생성

## 14-2. 미뤄도 되는 것

- 앱
- 실시간 채팅
- 견적 자동 비교
- 결제/에스크로
- 고급 CAD 편집 기능
- 정밀 비용 산출 엔진

---

## 15. 구현 우선순위

### Phase 1

- 주소 검색 + 프로젝트 생성 + 집 정보 카드
- 도면 후보 resolver + 수동 선택
- 공정 카탈로그/가이드 CMS
- 기본 전문가 목록/리드

### Phase 2

- 스타일 이미지 생성
- 공정 플래너 자동화
- 사진 질문 답변
- 전문가 추천 고도화

### Phase 3

- 근사 평면 생성
- 비용 리포트
- 견적 검토
- SEO 콘텐츠 자동 확장

---

# Codex용 구현 프롬프트

아래는 **Codex에 순차적으로 넣을 수 있는 프롬프트 세트**다.

---

## Prompt 1. 저장소 부트스트랩

```text
너는 시니어 풀스택 아키텍트이자 구현자다.
반셀프 인테리어 웹 서비스를 위한 monorepo를 생성하라.

목표:
- 웹 프론트엔드(Next.js App Router, TypeScript, Tailwind)
- 백엔드 API(Spring Boot, Java 21, Gradle, PostgreSQL, Redis, S3 연동 준비)
- docs 폴더에 설계 문서 저장
- docker-compose 로컬 실행
- 환경변수 예시 파일 제공
- lint/test/build 스크립트 제공

필수 조건:
1. 저장소 구조를 아래처럼 생성하라.
   - apps/web
   - apps/api
   - packages/shared-types
   - docs
   - infra
2. 프론트는 App Router 기준으로 라우트 골격을 만든다.
3. 백엔드는 모듈형 모놀리스 패키지 구조를 만든다.
4. 공통 응답 포맷, 에러 코드, 로깅, requestId 미들웨어를 구현하라.
5. DB 마이그레이션 도구(Flyway 또는 Liquibase)를 설정하라.
6. 코드 품질:
   - 프론트: ESLint, Prettier
   - 백엔드: spotless 또는 checkstyle
7. README에 실행 방법, 개발 규칙, 환경변수 설명을 자세히 적어라.

백엔드 패키지 구조:
- com.example.interior.common
- com.example.interior.auth
- com.example.interior.project
- com.example.interior.property
- com.example.interior.floorplan
- com.example.interior.process
- com.example.interior.visualqa
- com.example.interior.expert
- com.example.interior.integration
- com.example.interior.admin

산출물:
- 전체 디렉터리 생성
- 초기 빌드가 성공하는 코드
- 최소 샘플 API 1개(/api/v1/health)
- 최소 샘플 페이지 1개(/)
- docker-compose.yml
- .env.example
- apps/api/src/main/resources/db/migration 초기 파일
```

---

## Prompt 2. DB 스키마와 JPA 엔티티 구현

```text
이전 monorepo를 기반으로 PostgreSQL 스키마를 구현하라.

구현 대상 테이블:
- users
- project_members
- projects
- properties
- address_resolution_logs
- external_property_refs
- floor_plan_sources
- floor_plan_candidates
- floor_plan_files
- normalized_floor_plans
- normalized_plan_spaces
- normalized_plan_openings
- normalized_plan_walls
- plan_selection_events
- project_scopes
- style_presets
- project_style_selections
- generated_style_images
- process_catalogs
- process_guides
- process_checklists
- project_process_plans
- project_process_steps
- project_process_tasks
- visual_questions
- visual_question_images
- visual_answers
- expert_categories
- experts
- expert_category_links
- expert_service_regions
- expert_portfolios
- expert_leads
- expert_lead_events
- knowledge_articles
- faq_entries
- integration_call_logs
- ai_job_runs
- audit_logs

필수 조건:
1. Flyway migration SQL 작성
2. 각 테이블에 대응하는 JPA Entity 작성
3. Repository 작성
4. enum 값은 코드 상수 또는 enum 클래스로 분리
5. JSONB 컬럼은 적절히 매핑
6. soft delete 가 필요한 테이블은 deletedAt 컬럼까지 준비
7. 테스트:
   - repository 기본 save/find 테스트
   - migration 검증 테스트

추가:
- 인덱스와 unique constraint를 migration에 반영
- 공통 BaseEntity(createdAt, updatedAt) 적용
- UUID 전략 일관되게 유지
```

---

## Prompt 3. 주소 검색/집 식별 모듈 구현

```text
주소-first 온보딩을 구현하라.

백엔드 구현 범위:
- POST /api/v1/address/search
- POST /api/v1/address/detail-options
- POST /api/v1/property/resolve

구조:
- controller
- service
- external adapter
- dto
- mapper
- integration log 저장

외부 연동은 실제 호출 대신 우선 인터페이스 + mock adapter + stub response로 구현하라.
필요한 adapter 인터페이스:
- KakaoLocalClient
- JusoAddressClient
- KaptClient
- BuildingHubClient
- HousingHubClient
- VworldClient

비즈니스 로직:
1. 검색어 입력 시 주소/단지 후보를 반환
2. 동/층/호 추가 선택이 필요한 경우 detail-options 제공
3. resolve 호출 시 공동주택 여부, 단지 메타데이터, 평형 후보, 외부 참조키를 취합
4. 모든 외부 호출은 integration_call_logs 에 기록
5. 실패 시 provider별 graceful fallback 적용

프론트 구현 범위:
- 홈 검색 입력창
- 검색 결과 페이지
- 집 확정 UI
- 동/호 선택 UI
- “이 집으로 프로젝트 만들기” CTA

필수:
- 서버/클라이언트 타입 일치
- skeleton/loading/error 상태 구현
- 테스트용 mock data 준비
```

---

## Prompt 4. 프로젝트 생성 + 대시보드 구현

```text
주소 확정 이후 프로젝트 생성과 대시보드를 구현하라.

백엔드 API:
- POST /api/v1/projects
- GET /api/v1/projects
- GET /api/v1/projects/{projectId}
- PATCH /api/v1/projects/{projectId}
- POST /api/v1/projects/{projectId}/property

비즈니스 규칙:
1. 프로젝트 생성 시 project_scope 기본값도 함께 생성 가능
2. property 연결 후 프로젝트 홈 요약 카드 생성에 필요한 DTO 제공
3. 현재 단계(currentProcessStep)는 null 허용하되 이후 자동 생성될 수 있도록 설계
4. owner_user_id와 project_members OWNER 관계를 함께 생성

프론트 구현:
- 프로젝트 생성 모달
- 프로젝트 목록 페이지
- 프로젝트 홈 대시보드
  - 우리 집 요약 카드
  - 지금 해야 할 일
  - 최근 질문
  - 추천 전문가 placeholder
- 데이터 fetching/loading/error 처리

추가:
- 디자인은 shadcn/ui 또는 자체 공통 컴포넌트로 일관성 있게 구성
- route guard 준비
```

---

## Prompt 5. 도면 resolver + 후보 선택 UX 구현

```text
이 프로젝트의 핵심 모듈인 floor plan resolver를 구현하라.

백엔드 API:
- POST /api/v1/projects/{projectId}/floor-plans/resolve
- GET /api/v1/projects/{projectId}/floor-plans
- POST /api/v1/projects/{projectId}/floor-plans/{candidateId}/select
- POST /api/v1/projects/{projectId}/floor-plans/upload
- GET /api/v1/projects/{projectId}/floor-plans/{candidateId}/normalized

백엔드 설계:
1. FloorPlanResolutionService
2. 내부 단계:
   - project/property 로드
   - 내부 캐시 조회
   - official plan adapter 조회
   - licensed plan adapter 조회
   - 유사 도면 fallback
   - approximate generator fallback
3. confidence score 계산기 구현
4. floor_plan_candidates, floor_plan_files, normalized_floor_plans 저장
5. source provenance, license_status, access_scope 반드시 저장
6. 비동기 job 구조를 위한 application service 추상화 제공
7. 업로드 파일은 local mock storage 또는 S3 abstraction 사용

프론트 구현:
- 도면 후보 리스트
- 신뢰도 배지
- 도면 미리보기
- 후보 선택 버튼
- 근사 구조 안내 문구
- 업로드 fallback UI

중요:
- 실제 외부 호출은 mock으로 시작하되 adapter 인터페이스를 실제 연동 가능한 형태로 설계
- exact/high/approx/low 상태별 UI 분기 구현
- optimistic update 금지, 서버 응답 기준으로 상태 반영
```

---

## Prompt 6. 스타일 이미지 + 공정 플래너 구현

```text
스타일 이미지 생성과 공정 플래너를 구현하라.

백엔드 API:
- POST /api/v1/projects/{projectId}/styles/generate
- GET /api/v1/projects/{projectId}/styles/images
- POST /api/v1/projects/{projectId}/styles/images/{imageId}/like
- POST /api/v1/projects/{projectId}/process-plan/generate
- GET /api/v1/projects/{projectId}/process-plan
- GET /api/v1/projects/{projectId}/process-plan/steps/{stepKey}
- PATCH /api/v1/projects/{projectId}/process-plan/tasks/{taskId}

구현 규칙:
1. style_presets seed data migration 작성
2. process_catalogs, process_guides, process_checklists seed data 작성
3. process-plan 생성 시 프로젝트 범위, 거주 여부, 유지할 요소를 반영한 단계 분기 구현
4. 스타일 이미지 생성은 mock provider로 시작하되 prompt_text, model_name, metadata 저장
5. 각 이미지 카드에 난이도/예산영향/필요공정 계산 필드를 DTO에 포함
6. 공정 상세 페이지에는:
   - 목적
   - 시작 전 체크리스트
   - 결정 포인트
   - 셀프 가능 범위
   - 전문가 필수 범위
   - 실수 방지
   - 다음 단계 체크

프론트 구현:
- 스타일 페이지
- 이미지 그리드
- 좋아요
- “이 스타일로 계획 만들기”
- 공정 플래너 타임라인
- 공정 상세 페이지
- 체크리스트 완료 토글
```

---

## Prompt 7. 사진 질문답변 + 전문가 리드 구현

```text
사진 질문답변과 전문가 리드 생성을 구현하라.

백엔드 API:
- POST /api/v1/projects/{projectId}/visual-questions
- GET /api/v1/projects/{projectId}/visual-questions
- GET /api/v1/projects/{projectId}/visual-questions/{questionId}
- GET /api/v1/expert-categories
- GET /api/v1/experts
- GET /api/v1/experts/{expertId}
- POST /api/v1/projects/{projectId}/expert-leads

비즈니스 규칙:
1. visual question 생성 시 이미지 업로드 저장
2. VisualQaService는 다음 구조를 가진다:
   - 이미지 요약
   - 프로젝트/공정 문맥 결합
   - process guide/faq 기반 RAG placeholder
   - structured answer 생성
3. risk level 분류 구현
4. HIGH 이상은 expert_required=true 로 설정
5. 전문가 추천은 현재 공정, 지역, 예산, risk level을 반영
6. 리드 생성 시 프로젝트 요약/사진/질문 이력을 attachment_payload 에 포함 가능하게 설계

프론트 구현:
- 사진 업로드 UI
- 질문 등록 폼
- 답변 카드
- 관련 가이드 링크
- 전문가 추천 영역
- 리드 생성 폼
- 전문가 목록/상세 페이지

추가:
- 관리자용 전문가 승인 상태 반영
- PENDING/SUSPENDED 전문가 노출 금지
```

---

## Prompt 8. 관리자/CMS/운영 마무리

```text
운영 가능한 MVP 수준으로 관리자 기능과 관측성을 추가하라.

구현 범위:
- 관리자 로그인 가드(간단 mock 또는 role 기반)
- process guide CRUD
- faq CRUD
- knowledge article CRUD
- experts 승인/정지
- floor plan candidate 검수 화면
- integration logs 조회 화면
- ai_job_runs 조회 화면

필수 조건:
1. 관리자 라우트 분리
2. audit_logs 저장
3. 중요한 변경 작업은 모두 감사 로그 기록
4. 실패/성공 토스트, 폼 검증, 접근제어 구현
5. README에 운영자 사용법 추가
6. seed 데이터와 demo 시나리오 제공

추가:
- 예시 계정 2개(USER, ADMIN)
- 테스트 데이터 주입 스크립트
- 로컬에서 데모 가능한 상태로 마무리
```

---

## 16. Codex에게 반드시 같이 전달할 구현 원칙

```text
구현 원칙:
- “주소-first, 도면-fallback” 구조를 절대 무시하지 말 것
- 외부 API는 adapter interface 뒤에 숨길 것
- provenance, license_status, confidence_score 를 core model 로 유지할 것
- 고위험 진단성 답변은 확정 표현을 피할 것
- 프론트는 처음부터 모바일 반응형으로 작성하되, 웹 우선 UX를 유지할 것
- 장황한 CSS보다 재사용 가능한 컴포넌트 우선
- 서버 에러/외부 연동 실패를 정상적인 제품 흐름으로 취급할 것
- mock provider로 먼저 완성하고, 이후 실제 API adapter 를 교체 가능하게 설계할 것
- 테스트 가능성을 항상 우선할 것
```

---

## 17. 최종 구현 순서 추천

1. monorepo 부트스트랩
2. DB/migration
3. 주소 검색/프로젝트 생성
4. 도면 resolver
5. 프로젝트 홈/집 정보 페이지
6. 공정 플래너
7. 스타일 이미지
8. 사진 질문
9. 전문가 리드
10. 관리자/CMS
11. 실제 외부 API 연동
12. SEO/콘텐츠 확장

---

## 18. 마지막 제품 판단

이 서비스의 성공 여부는 이미지 생성 품질보다

1. 주소 입력이 얼마나 부드러운지
2. 도면이 얼마나 자연스럽게 확보/근사화되는지
3. 공정 플래너가 얼마나 실용적인지
4. 사진 질문에서 얼마나 “도움이 됐다”는 느낌을 주는지
5. 전문가 연결이 얼마나 빠르게 성사되는지
   에 달려 있다.

즉 개발 우선순위는 반드시  
**주소 → 도면 → 공정 → 질문 → 전문가 → 스타일** 순으로 의사결정해야 한다.
스타일 이미지는 유입과 공유를 만들지만,
매출은 도면/공정/전문가에서 발생한다.
