create table expert_categories (
    id uuid primary key default gen_random_uuid(),
    key varchar(50) not null unique,
    name varchar(100) not null,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table experts (
    id uuid primary key default gen_random_uuid(),
    company_name varchar(150) not null,
    contact_name varchar(100) not null,
    phone varchar(30),
    email varchar(255),
    business_no varchar(30),
    license_info jsonb,
    intro_text text,
    min_budget integer,
    max_budget integer,
    partial_work_supported boolean not null default true,
    semi_self_collaboration_supported boolean not null default true,
    response_score numeric(5, 2),
    review_score numeric(5, 2),
    status varchar(30) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table expert_category_links (
    id uuid primary key default gen_random_uuid(),
    expert_id uuid not null references experts(id),
    expert_category_id uuid not null references expert_categories(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (expert_id, expert_category_id)
);

create table expert_service_regions (
    id uuid primary key default gen_random_uuid(),
    expert_id uuid not null references experts(id),
    sido varchar(50) not null,
    sigungu varchar(50),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table expert_portfolios (
    id uuid primary key default gen_random_uuid(),
    expert_id uuid not null references experts(id),
    title varchar(150) not null,
    description text,
    storage_key varchar(255),
    metadata jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table expert_leads (
    id uuid primary key default gen_random_uuid(),
    project_id uuid not null references projects(id),
    expert_id uuid not null references experts(id),
    requested_category_id uuid not null references expert_categories(id),
    lead_status varchar(30) not null,
    budget_min integer,
    budget_max integer,
    desired_start_date date,
    message text,
    attachment_payload jsonb,
    created_by_user_id uuid not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table expert_lead_events (
    id uuid primary key default gen_random_uuid(),
    expert_lead_id uuid not null references expert_leads(id),
    event_type varchar(30) not null,
    payload jsonb,
    created_at timestamptz not null default now()
);

create index ix_expert_categories_active on expert_categories(active);
create index ix_experts_status on experts(status);
create index ix_expert_category_links_expert_id on expert_category_links(expert_id);
create index ix_expert_category_links_category_id on expert_category_links(expert_category_id);
create index ix_expert_service_regions_expert_id on expert_service_regions(expert_id);
create index ix_expert_service_regions_sido_sigungu on expert_service_regions(sido, sigungu);
create index ix_expert_portfolios_expert_id on expert_portfolios(expert_id);
create index ix_expert_leads_project_id on expert_leads(project_id);
create index ix_expert_leads_expert_id on expert_leads(expert_id);
create index ix_expert_leads_status on expert_leads(lead_status);
create index ix_expert_lead_events_lead_id on expert_lead_events(expert_lead_id);

insert into expert_categories (key, name, active) values
    ('WATERPROOFING', '누수/방수 점검', true),
    ('ELECTRICAL', '전기 점검', true),
    ('SURFACE_FINISH', '도배/필름/도장', true),
    ('DEMOLITION', '철거/목공', true),
    ('TILE_BATH', '타일/욕실 보수', true);

insert into experts (
    company_name,
    contact_name,
    phone,
    email,
    business_no,
    license_info,
    intro_text,
    min_budget,
    max_budget,
    partial_work_supported,
    semi_self_collaboration_supported,
    response_score,
    review_score,
    status
) values
    ('한빛 누수 솔루션', '김도윤', '02-111-2200', 'leak@hanbit.example.com', '110-88-00001', '{"type":"건설업","note":"누수 탐지 장비 보유"}'::jsonb, '욕실, 발코니, 결로 점검과 부분 보수를 함께 진행한다.', 800000, 6000000, true, true, 92.0, 4.8, 'ACTIVE'),
    ('세운 전기 설비', '박지훈', '02-333-4400', 'power@sewoon.example.com', '110-88-00002', '{"type":"전기공사업","note":"주거 전기 증설 경험 다수"}'::jsonb, '콘센트, 스위치, 차단기, 조명 회로 분리를 주로 맡는다.', 500000, 5000000, true, true, 95.0, 4.9, 'ACTIVE'),
    ('바른 마감 스튜디오', '최유진', '031-555-6600', 'finish@bareun.example.com', '110-88-00003', '{"type":"실내건축","note":"도배와 필름 부분 보수 가능"}'::jsonb, '도배, 필름, 페인트를 부분 공사와 반셀프 협업 중심으로 진행한다.', 1200000, 7000000, true, true, 89.0, 4.7, 'ACTIVE'),
    ('도심 철거 목공', '이준호', '02-777-8800', 'demo@dosim.example.com', '110-88-00004', '{"type":"철거/목공","note":"철거 후 보양 연계"}'::jsonb, '부분 철거와 목공 보수, 보양 계획 수립을 함께 지원한다.', 1500000, 9000000, false, true, 84.0, 4.5, 'ACTIVE'),
    ('청담 욕실 타일 팀', '정세라', '02-999-1122', 'tile@bath.example.com', '110-88-00005', '{"type":"타일/방수","note":"욕실 하자 보수 특화"}'::jsonb, '욕실 타일, 줄눈, 실리콘, 방수 보수 중심의 팀이다.', 900000, 6500000, true, true, 90.0, 4.8, 'ACTIVE'),
    ('대기중 샘플 업체', '홍샘플', '02-000-0000', 'pending@example.com', '110-88-99999', '{"type":"샘플"}'::jsonb, '승인 대기 상태의 샘플 업체다.', 1000000, 3000000, true, true, 50.0, 3.8, 'PENDING');

insert into expert_category_links (expert_id, expert_category_id) values
    ((select id from experts where company_name = '한빛 누수 솔루션'), (select id from expert_categories where key = 'WATERPROOFING')),
    ((select id from experts where company_name = '한빛 누수 솔루션'), (select id from expert_categories where key = 'TILE_BATH')),
    ((select id from experts where company_name = '세운 전기 설비'), (select id from expert_categories where key = 'ELECTRICAL')),
    ((select id from experts where company_name = '바른 마감 스튜디오'), (select id from expert_categories where key = 'SURFACE_FINISH')),
    ((select id from experts where company_name = '도심 철거 목공'), (select id from expert_categories where key = 'DEMOLITION')),
    ((select id from experts where company_name = '청담 욕실 타일 팀'), (select id from expert_categories where key = 'TILE_BATH')),
    ((select id from experts where company_name = '청담 욕실 타일 팀'), (select id from expert_categories where key = 'WATERPROOFING')),
    ((select id from experts where company_name = '대기중 샘플 업체'), (select id from expert_categories where key = 'SURFACE_FINISH'));

insert into expert_service_regions (expert_id, sido, sigungu) values
    ((select id from experts where company_name = '한빛 누수 솔루션'), '서울특별시', '강남구'),
    ((select id from experts where company_name = '한빛 누수 솔루션'), '서울특별시', '송파구'),
    ((select id from experts where company_name = '세운 전기 설비'), '서울특별시', '강남구'),
    ((select id from experts where company_name = '세운 전기 설비'), '서울특별시', '서초구'),
    ((select id from experts where company_name = '바른 마감 스튜디오'), '경기도', '성남시'),
    ((select id from experts where company_name = '바른 마감 스튜디오'), '서울특별시', '송파구'),
    ((select id from experts where company_name = '도심 철거 목공'), '서울특별시', '강동구'),
    ((select id from experts where company_name = '청담 욕실 타일 팀'), '서울특별시', '강남구'),
    ((select id from experts where company_name = '청담 욕실 타일 팀'), '서울특별시', '서초구'),
    ((select id from experts where company_name = '대기중 샘플 업체'), '서울특별시', '강남구');

insert into expert_portfolios (expert_id, title, description, storage_key, metadata) values
    ((select id from experts where company_name = '한빛 누수 솔루션'), '욕실 결로 점검 및 부분 보수', '결로 원인 진단 후 실리콘과 환기 개선을 함께 제안한 사례다.', 'mock://portfolio/leak-1', '{"space":"BATHROOM","beforeAfter":true}'::jsonb),
    ((select id from experts where company_name = '세운 전기 설비'), '주방 전기 회로 분리', '가전 사용량 증가에 맞춰 전용 회로를 정리한 사례다.', 'mock://portfolio/electrical-1', '{"space":"KITCHEN","beforeAfter":true}'::jsonb),
    ((select id from experts where company_name = '바른 마감 스튜디오'), '거실 도배/필름 부분 공사', '입주 중 반셀프 일정에 맞춰 도배와 필름을 나눠 진행한 사례다.', 'mock://portfolio/finish-1', '{"space":"LIVING_ROOM","beforeAfter":true}'::jsonb),
    ((select id from experts where company_name = '청담 욕실 타일 팀'), '욕실 타일 보수와 실리콘 교체', '누수 의심 구간 중심으로 최소 범위 보수를 진행한 사례다.', 'mock://portfolio/tile-1', '{"space":"BATHROOM","beforeAfter":true}'::jsonb);
