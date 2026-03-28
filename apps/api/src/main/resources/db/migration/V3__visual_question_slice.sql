create table visual_questions (
    id uuid primary key default gen_random_uuid(),
    project_id uuid not null references projects(id),
    question_text text not null,
    process_step_key varchar(80),
    space_type varchar(50) not null,
    status varchar(30) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table visual_question_images (
    id uuid primary key default gen_random_uuid(),
    question_id uuid not null references visual_questions(id),
    file_name varchar(255) not null,
    content_type varchar(120),
    storage_path varchar(500) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table visual_answers (
    id uuid primary key default gen_random_uuid(),
    question_id uuid not null unique references visual_questions(id),
    risk_level varchar(30) not null,
    observed_text text not null,
    possible_causes_text text not null,
    next_checks_text text not null,
    proceed_recommendation_text text not null,
    expert_required boolean not null,
    confidence_score numeric(5, 2) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index ix_visual_questions_project_id on visual_questions(project_id);
create index ix_visual_questions_status on visual_questions(status);
create index ix_visual_question_images_question_id on visual_question_images(question_id);
create index ix_visual_answers_question_id on visual_answers(question_id);
