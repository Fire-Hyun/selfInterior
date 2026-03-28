create table style_presets (
    id uuid primary key default gen_random_uuid(),
    key varchar(50) not null unique,
    name varchar(100) not null,
    description text,
    prompt_template text not null,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table project_style_selections (
    id uuid primary key default gen_random_uuid(),
    project_id uuid not null references projects(id),
    style_preset_id uuid not null references style_presets(id),
    space_type varchar(50) not null,
    priority integer not null default 1,
    selected boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table generated_style_images (
    id uuid primary key default gen_random_uuid(),
    project_id uuid not null references projects(id),
    style_preset_id uuid references style_presets(id),
    space_type varchar(50) not null,
    prompt_text text not null,
    negative_prompt_text text,
    generation_status varchar(30) not null,
    storage_key varchar(255),
    thumbnail_key varchar(255),
    seed varchar(100),
    model_name varchar(100),
    metadata jsonb,
    liked boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index ix_style_presets_active on style_presets(active);
create index ix_project_style_selections_project_id on project_style_selections(project_id);
create index ix_project_style_selections_space_type on project_style_selections(space_type);
create index ix_generated_style_images_project_id on generated_style_images(project_id);
create index ix_generated_style_images_space_type on generated_style_images(space_type);
create index ix_generated_style_images_liked on generated_style_images(liked);

insert into style_presets (key, name, description, prompt_template, active) values
    (
        'WHITE_MINIMAL',
        '화이트 미니멀',
        '밝은 톤과 정돈된 수납, 과한 장식이 없는 기본형 무드',
        'clean white minimal interior for {{spaceType}}, soft natural light, practical storage, calm family home, keep {{keepItems}}',
        true
    ),
    (
        'WARM_NATURAL',
        '웜 내추럴',
        '우드와 베이지 중심의 편안한 생활형 무드',
        'warm natural interior for {{spaceType}}, oak wood, beige texture, cozy but practical mood, keep {{keepItems}}',
        true
    ),
    (
        'MODERN_CONTRAST',
        '모던 콘트라스트',
        '밝은 바탕에 블랙 포인트를 섞은 선명한 현대적 무드',
        'modern contrast interior for {{spaceType}}, bright base, black accents, crisp lines, balanced budget, keep {{keepItems}}',
        true
    );
