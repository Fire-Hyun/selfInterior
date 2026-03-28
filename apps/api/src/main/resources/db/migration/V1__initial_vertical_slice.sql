create extension if not exists "pgcrypto";

create table users (
    id uuid primary key,
    email varchar(255) not null unique,
    password_hash varchar(255),
    provider varchar(30) not null default 'email',
    name varchar(100) not null,
    phone varchar(30),
    role varchar(30) not null default 'USER',
    marketing_consent boolean not null default false,
    last_login_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table projects (
    id uuid primary key default gen_random_uuid(),
    owner_user_id uuid not null references users(id),
    title varchar(150) not null,
    status varchar(30) not null,
    project_type varchar(30) not null,
    living_status varchar(30) not null,
    budget_min integer,
    budget_max integer,
    currency varchar(10) not null default 'KRW',
    current_process_step varchar(50),
    onboarding_completed boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table project_members (
    id uuid primary key default gen_random_uuid(),
    project_id uuid not null references projects(id),
    user_id uuid not null references users(id),
    member_role varchar(30) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (project_id, user_id)
);

create table project_scopes (
    id uuid primary key default gen_random_uuid(),
    project_id uuid not null unique references projects(id),
    scope_type varchar(30) not null,
    spaces_targeted jsonb,
    keep_items jsonb,
    self_work_items jsonb,
    desired_style_keywords jsonb,
    schedule_start_target date,
    schedule_end_target date,
    special_notes text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table properties (
    id uuid primary key default gen_random_uuid(),
    project_id uuid not null unique references projects(id),
    property_type varchar(30) not null,
    country_code varchar(10) not null default 'KR',
    sido varchar(50),
    sigungu varchar(50),
    eup_myeon_dong varchar(80),
    road_address varchar(255),
    jibun_address varchar(255),
    detail_address varchar(100),
    apartment_name varchar(150),
    building_no varchar(50),
    dong_no varchar(50),
    ho_no varchar(50),
    postal_code varchar(20),
    lat numeric(10, 7),
    lng numeric(10, 7),
    completion_year integer,
    approval_date date,
    household_count integer,
    supply_area_m2 numeric(10, 2),
    exclusive_area_m2 numeric(10, 2),
    room_count integer,
    bathroom_count integer,
    balcony_count integer,
    heating_type varchar(50),
    raw_summary jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table address_resolution_logs (
    id uuid primary key default gen_random_uuid(),
    project_id uuid references projects(id),
    input_query varchar(255) not null,
    normalized_road_address varchar(255),
    normalized_jibun_address varchar(255),
    road_code varchar(30),
    building_main_no varchar(20),
    building_sub_no varchar(20),
    legal_dong_code varchar(20),
    source varchar(50) not null,
    confidence_score numeric(5, 2) not null,
    raw_payload jsonb not null,
    created_at timestamptz not null default now()
);

create table external_property_refs (
    id uuid primary key default gen_random_uuid(),
    property_id uuid not null references properties(id),
    provider varchar(50) not null,
    external_key varchar(255) not null,
    ref_type varchar(50) not null,
    metadata jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (provider, external_key, ref_type)
);

create table integration_call_logs (
    id uuid primary key default gen_random_uuid(),
    provider varchar(50) not null,
    operation varchar(100) not null,
    request_id varchar(100),
    status_code varchar(30),
    success boolean not null,
    latency_ms integer,
    error_message text,
    request_meta jsonb,
    response_meta jsonb,
    created_at timestamptz not null default now()
);

create table floor_plan_sources (
    id uuid primary key default gen_random_uuid(),
    provider varchar(50) not null,
    license_status varchar(30) not null,
    access_scope varchar(30) not null,
    provider_doc_ref varchar(255),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table floor_plan_candidates (
    id uuid primary key default gen_random_uuid(),
    project_id uuid not null references projects(id),
    property_id uuid not null references properties(id),
    floor_plan_source_id uuid not null references floor_plan_sources(id),
    provider_plan_key varchar(255),
    source_type varchar(30) not null,
    source varchar(100) not null,
    match_type varchar(30) not null,
    confidence_score numeric(5, 2) not null,
    confidence_grade varchar(20) not null,
    exclusive_area_m2 numeric(10, 2),
    supply_area_m2 numeric(10, 2),
    room_count integer,
    bathroom_count integer,
    layout_label varchar(100),
    is_selected boolean not null default false,
    selection_reason varchar(255),
    raw_payload jsonb,
    raw_payload_ref varchar(255),
    normalized_plan_ref varchar(255),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table normalized_floor_plans (
    id uuid primary key default gen_random_uuid(),
    floor_plan_candidate_id uuid not null unique references floor_plan_candidates(id),
    normalization_status varchar(30) not null,
    plan_json jsonb not null,
    uncertainty_json jsonb,
    manual_check_items jsonb,
    normalized_by varchar(30) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index ix_projects_owner_user_id on projects(owner_user_id);
create index ix_projects_status on projects(status);
create index ix_projects_project_type on projects(project_type);
create index ix_properties_project_id on properties(project_id);
create index ix_properties_apartment_name on properties(apartment_name);
create index ix_address_resolution_logs_project_id on address_resolution_logs(project_id);
create index ix_address_resolution_logs_road_code on address_resolution_logs(road_code);
create index ix_floor_plan_candidates_project_id on floor_plan_candidates(project_id);
create index ix_floor_plan_candidates_property_id on floor_plan_candidates(property_id);
create index ix_floor_plan_candidates_confidence_score on floor_plan_candidates(confidence_score);
create index ix_floor_plan_candidates_is_selected on floor_plan_candidates(is_selected);
create index gin_normalized_floor_plans_plan_json on normalized_floor_plans using gin(plan_json);

insert into users (
    id,
    email,
    password_hash,
    provider,
    name,
    phone,
    role,
    marketing_consent,
    created_at,
    updated_at
) values (
    '11111111-1111-1111-1111-111111111111',
    'owner@selfinterior.local',
    null,
    'seed',
    '기본 소유자',
    null,
    'USER',
    false,
    now(),
    now()
);
