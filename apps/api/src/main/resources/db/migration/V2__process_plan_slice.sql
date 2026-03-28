create table process_catalogs (
    id uuid primary key default gen_random_uuid(),
    step_key varchar(80) not null unique,
    title varchar(150) not null,
    description text not null,
    sort_order integer not null,
    default_duration_days integer not null,
    applicable_project_types jsonb not null,
    applicable_living_statuses jsonb not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table process_guides (
    id uuid primary key default gen_random_uuid(),
    process_catalog_id uuid not null unique references process_catalogs(id),
    purpose_text text not null,
    start_check_intro text not null,
    decision_points jsonb not null,
    self_work_text text not null,
    expert_required_text text not null,
    mistakes_text text not null,
    next_step_checks jsonb not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table process_checklists (
    id uuid primary key default gen_random_uuid(),
    process_catalog_id uuid not null references process_catalogs(id),
    task_group varchar(30) not null,
    item_order integer not null,
    title varchar(255) not null,
    description text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table project_process_plans (
    id uuid primary key default gen_random_uuid(),
    project_id uuid not null unique references projects(id),
    plan_status varchar(30) not null,
    generated_from_floor_plan_id uuid not null references floor_plan_candidates(id),
    current_step_key varchar(80),
    generated_summary jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table project_process_steps (
    id uuid primary key default gen_random_uuid(),
    process_plan_id uuid not null references project_process_plans(id),
    process_catalog_id uuid not null references process_catalogs(id),
    step_key varchar(80) not null,
    title varchar(150) not null,
    status varchar(30) not null,
    sort_order integer not null,
    duration_days integer not null,
    is_required boolean not null default true,
    description text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (process_plan_id, step_key)
);

create table project_process_tasks (
    id uuid primary key default gen_random_uuid(),
    project_process_step_id uuid not null references project_process_steps(id),
    task_group varchar(30) not null,
    item_order integer not null,
    title varchar(255) not null,
    description text,
    completed boolean not null default false,
    completed_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index ix_process_catalogs_sort_order on process_catalogs(sort_order);
create index ix_process_checklists_catalog_id on process_checklists(process_catalog_id);
create index ix_project_process_plans_project_id on project_process_plans(project_id);
create index ix_project_process_steps_plan_id on project_process_steps(process_plan_id);
create index ix_project_process_steps_status on project_process_steps(status);
create index ix_project_process_tasks_step_id on project_process_tasks(project_process_step_id);
create index ix_project_process_tasks_completed on project_process_tasks(completed);

insert into process_catalogs (
    step_key,
    title,
    description,
    sort_order,
    default_duration_days,
    applicable_project_types,
    applicable_living_statuses
) values
    (
        'SITE_PREP',
        '현장 준비',
        '공사 시작 전 범위 확정, 보양, 일정 조율을 정리하는 단계다.',
        10,
        2,
        '["FULL","PARTIAL"]'::jsonb,
        '["BEFORE_MOVE_IN","OCCUPIED"]'::jsonb
    ),
    (
        'DEMOLITION',
        '철거',
        '철거 범위와 폐기물 처리, 구조 안전 확인을 준비하는 단계다.',
        20,
        3,
        '["FULL"]'::jsonb,
        '["BEFORE_MOVE_IN"]'::jsonb
    ),
    (
        'ELECTRICAL',
        '전기',
        '조명, 스위치, 콘센트, 회로 분리를 점검하고 확정하는 단계다.',
        30,
        3,
        '["FULL","PARTIAL"]'::jsonb,
        '["BEFORE_MOVE_IN","OCCUPIED"]'::jsonb
    ),
    (
        'SURFACE_FINISH',
        '표면 마감',
        '도배, 필름, 페인트, 마루 등 마감 결정을 정리하는 단계다.',
        40,
        4,
        '["FULL","PARTIAL"]'::jsonb,
        '["BEFORE_MOVE_IN","OCCUPIED"]'::jsonb
    ),
    (
        'FINAL_FIXTURE',
        '마감 점검',
        '조명, 가구, 청소와 하자 확인을 정리하는 단계다.',
        50,
        2,
        '["FULL","PARTIAL","ISSUE_CHECK"]'::jsonb,
        '["BEFORE_MOVE_IN","OCCUPIED"]'::jsonb
    ),
    (
        'ISSUE_DIAGNOSIS',
        '문제 점검',
        '현장 이슈 원인 후보와 추가 확인 포인트를 정리하는 단계다.',
        15,
        1,
        '["ISSUE_CHECK"]'::jsonb,
        '["BEFORE_MOVE_IN","OCCUPIED"]'::jsonb
    );

insert into process_guides (
    process_catalog_id,
    purpose_text,
    start_check_intro,
    decision_points,
    self_work_text,
    expert_required_text,
    mistakes_text,
    next_step_checks
) values
    (
        (select id from process_catalogs where step_key = 'SITE_PREP'),
        '공사 범위와 순서를 정리해 뒤 단계의 혼선을 줄인다.',
        '도면과 현재 집 상태를 나란히 두고 공사 범위, 예산, 보양 여부를 먼저 맞춘다.',
        '["남길 품목 확정","셀프 작업 범위 확정","입주 일정과 공사 시작일 정렬"]'::jsonb,
        '기초 실측, 가구 비우기, 보양 범위 기록은 셀프로 준비할 수 있다.',
        '입주민 공지, 공용부 보양, 폐기물 동선 협의는 전문가 또는 관리사무소 협의가 필요하다.',
        '범위를 확정하지 않고 바로 시공 견적을 잡으면 일정과 비용이 흔들리기 쉽다.',
        '["철거 전 사진 보관","전기 변경 요청 위치 메모"]'::jsonb
    ),
    (
        (select id from process_catalogs where step_key = 'DEMOLITION'),
        '철거 범위와 구조 안전 리스크를 먼저 고정한다.',
        '철거 대상, 구조벽 가능성, 폐기물 배출 계획을 체크한다.',
        '["철거 범위 확정","구조벽 여부 재확인","폐기물 처리 방식"]'::jsonb,
        '철거 전 사진 기록과 철거 제외 품목 표시를 셀프로 준비할 수 있다.',
        '구조벽 판단, 설비 배관 절단 여부, 대형 철거는 전문가 검토가 필요하다.',
        '구조벽을 확정 표현으로 판단하거나, 철거 제외 품목 표시 없이 공사를 시작하면 위험하다.',
        '["철거 후 전기/설비 추가 작업 범위 재확인"]'::jsonb
    ),
    (
        (select id from process_catalogs where step_key = 'ELECTRICAL'),
        '전기 위치와 회로 계획을 마감 전에 고정한다.',
        '현재 가전 배치와 조명 계획을 먼저 정리하고 회로 분리 필요성을 확인한다.',
        '["콘센트 추가 위치","스위치 묶음 방식","주방 가전 전용 회로 여부"]'::jsonb,
        '사용 패턴 기록과 가전 위치 스케치는 셀프로 할 수 있다.',
        '배선 증설, 차단기 작업, 에어컨 전용선 공사는 전기 전문가가 필요하다.',
        '가전 위치를 확정하지 않고 전기 공사를 먼저 진행하면 재작업 가능성이 높다.',
        '["마감재 두께 반영","조명 스위치 최종 위치 재확인"]'::jsonb
    ),
    (
        (select id from process_catalogs where step_key = 'SURFACE_FINISH'),
        '도배, 필름, 페인트, 마루 순서를 정리하고 마감 선택을 확정한다.',
        '벽면 상태와 유지 품목을 보고 어떤 마감이 필요한지 분류한다.',
        '["도배/페인트 선택","마루 교체 범위","욕실 외 습식 보수 여부"]'::jsonb,
        '샘플 비교, 톤앤무드 정리, 유지할 가구 선별은 셀프로 할 수 있다.',
        '바탕면 보수, 습식 하자 보수, 면 정리 불량 판단은 전문가가 필요하다.',
        '샘플 확인 없이 색상만 보고 발주하면 톤 차이와 재시공 이슈가 생기기 쉽다.',
        '["조명/가구 설치 전 오염 체크","마감 하자 사진 기록"]'::jsonb
    ),
    (
        (select id from process_catalogs where step_key = 'FINAL_FIXTURE'),
        '마감 후 조명, 가구, 청소, 하자 체크를 마무리한다.',
        '설치 품목과 잔손볼 항목을 먼저 리스트업하고 입주 전 검수를 준비한다.',
        '["가구 반입 일정","조명/커튼 설치 순서","하자 체크 범위"]'::jsonb,
        '소품 배치, 청소 체크, 하자 사진 정리는 셀프로 할 수 있다.',
        '전기 재점검, 실리콘 보수, 설비 누수 확인은 전문가 검토가 필요하다.',
        '하자 체크 없이 바로 입주하면 재방문 비용과 일정이 커질 수 있다.',
        '["하자 보수 요청 목록 정리","입주 전 최종 점등 테스트"]'::jsonb
    ),
    (
        (select id from process_catalogs where step_key = 'ISSUE_DIAGNOSIS'),
        '문제 원인 후보와 다음 점검 포인트를 빠르게 정리한다.',
        '사진, 위치, 발생 시점, 최근 공사 이력을 먼저 정리한다.',
        '["문제 공간 확정","발생 조건 메모","추가 사진 확보 여부"]'::jsonb,
        '증상 기록과 사진 정리는 셀프로 할 수 있다.',
        '누수, 전기, 가스, 구조 문제의 확정 판단은 전문가 점검이 필요하다.',
        '원인을 단정하고 바로 시공을 진행하면 오진 비용이 커질 수 있다.',
        '["필요 시 전문가 문의 준비","보수 범위 최소화 검토"]'::jsonb
    );

insert into process_checklists (process_catalog_id, task_group, item_order, title, description) values
    ((select id from process_catalogs where step_key = 'SITE_PREP'), 'PREPARE', 10, '도면과 현재 집 상태 비교', '선택된 도면 후보와 현재 집 상태를 같이 보며 범위를 체크한다.'),
    ((select id from process_catalogs where step_key = 'SITE_PREP'), 'DECISION', 20, '남길 품목 표시', '붙박이장, 창호, 가전 등 유지 품목을 확정한다.'),
    ((select id from process_catalogs where step_key = 'SITE_PREP'), 'NEXT', 30, '관리사무소 공지 여부 확인', '공용부 보양과 공사 공지 절차를 체크한다.'),
    ((select id from process_catalogs where step_key = 'DEMOLITION'), 'PREPARE', 10, '철거 범위 라벨링', '철거 대상과 제외 품목을 사진과 메모로 남긴다.'),
    ((select id from process_catalogs where step_key = 'DEMOLITION'), 'DECISION', 20, '구조벽 가능성 확인', '확정 판단이 아니라 추가 확인 필요 여부를 정리한다.'),
    ((select id from process_catalogs where step_key = 'DEMOLITION'), 'NEXT', 30, '폐기물 반출 동선 점검', '엘리베이터, 배출 시간, 폐기물 처리 방식을 확인한다.'),
    ((select id from process_catalogs where step_key = 'ELECTRICAL'), 'PREPARE', 10, '가전 배치 메모', 'TV, 냉장고, 에어컨, 공유기 위치를 먼저 적는다.'),
    ((select id from process_catalogs where step_key = 'ELECTRICAL'), 'DECISION', 20, '콘센트/스위치 위치 확정', '추가 위치와 높이를 정리한다.'),
    ((select id from process_catalogs where step_key = 'ELECTRICAL'), 'NEXT', 30, '조명 회로 분리 확인', '주요 공간별 회로 분리 필요성을 체크한다.'),
    ((select id from process_catalogs where step_key = 'SURFACE_FINISH'), 'PREPARE', 10, '마감 샘플 수집', '도배, 필름, 페인트, 마루 샘플을 비교한다.'),
    ((select id from process_catalogs where step_key = 'SURFACE_FINISH'), 'DECISION', 20, '공간별 마감 방식 확정', '벽, 바닥, 문짝 등 적용 대상을 확정한다.'),
    ((select id from process_catalogs where step_key = 'SURFACE_FINISH'), 'NEXT', 30, '시공 전 오염 방지 계획', '작업 순서와 보호 대상을 정리한다.'),
    ((select id from process_catalogs where step_key = 'FINAL_FIXTURE'), 'PREPARE', 10, '잔손볼 목록 정리', '마감 후 보수 항목을 한 번에 모은다.'),
    ((select id from process_catalogs where step_key = 'FINAL_FIXTURE'), 'DECISION', 20, '입주 전 하자 체크 범위 확정', '조명, 실리콘, 마감 찍힘 등을 확인한다.'),
    ((select id from process_catalogs where step_key = 'FINAL_FIXTURE'), 'NEXT', 30, '최종 점등/환기 테스트', '입주 전 한 번 더 점검한다.'),
    ((select id from process_catalogs where step_key = 'ISSUE_DIAGNOSIS'), 'PREPARE', 10, '증상 사진과 시점 기록', '언제부터, 어떤 조건에서 문제가 생기는지 기록한다.'),
    ((select id from process_catalogs where step_key = 'ISSUE_DIAGNOSIS'), 'DECISION', 20, '원인 후보 우선순위 정리', '누수, 결로, 전기, 마감 하자 후보를 정리한다.'),
    ((select id from process_catalogs where step_key = 'ISSUE_DIAGNOSIS'), 'NEXT', 30, '전문가 점검 필요 여부 결정', '위험도가 높으면 바로 전문가 문의로 넘긴다.');
