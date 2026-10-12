-- Idempotent migrations for existing Docker volumes (prod profile uses ddl-auto=validate).
-- Fresh installs already apply classpath schema.sql via Postgres init; this script is for upgrades.

ALTER TABLE interview_invites ADD COLUMN IF NOT EXISTS proposed_interview_at TIMESTAMP;
ALTER TABLE interview_invites ADD COLUMN IF NOT EXISTS meeting_link VARCHAR(500);
ALTER TABLE interview_invites ADD COLUMN IF NOT EXISTS schedule_status VARCHAR(30);

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS phone VARCHAR(30);

ALTER TABLE anonymous_profiles ADD COLUMN IF NOT EXISTS study_area VARCHAR(150);

CREATE TABLE IF NOT EXISTS profile_preferred_modalities (
    profile_id UUID NOT NULL REFERENCES anonymous_profiles(id),
    modality VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS profile_preferred_employment_types (
    profile_id UUID NOT NULL REFERENCES anonymous_profiles(id),
    employment_type VARCHAR(30) NOT NULL
);

ALTER TABLE candidate_experiences ADD COLUMN IF NOT EXISTS seniority_level VARCHAR(50);
ALTER TABLE candidate_experiences ADD COLUMN IF NOT EXISTS start_month INT;
ALTER TABLE candidate_experiences ADD COLUMN IF NOT EXISTS start_year INT;
ALTER TABLE candidate_experiences ADD COLUMN IF NOT EXISTS end_month INT;
ALTER TABLE candidate_experiences ADD COLUMN IF NOT EXISTS end_year INT;
ALTER TABLE candidate_experiences ADD COLUMN IF NOT EXISTS is_current BOOLEAN DEFAULT FALSE;

-- Backfill new experience columns without depending on legacy duration_months
UPDATE candidate_experiences
SET seniority_level = COALESCE(seniority_level, 'pleno'),
    start_month = COALESCE(start_month, 1),
    start_year = COALESCE(start_year, EXTRACT(YEAR FROM CURRENT_DATE)::INT - 1),
    is_current = COALESCE(is_current, FALSE)
WHERE seniority_level IS NULL OR start_month IS NULL OR start_year IS NULL OR is_current IS NULL;

CREATE TABLE IF NOT EXISTS candidate_languages (
    id BIGSERIAL PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES anonymous_profiles(id),
    language_name VARCHAR(50) NOT NULL,
    language_level VARCHAR(50) NOT NULL
);

ALTER TABLE job_vacancies ADD COLUMN IF NOT EXISTS company_name VARCHAR(255);
ALTER TABLE job_vacancies ADD COLUMN IF NOT EXISTS work_modality VARCHAR(30);
ALTER TABLE job_vacancies ADD COLUMN IF NOT EXISTS employment_type VARCHAR(30);
ALTER TABLE job_vacancies ADD COLUMN IF NOT EXISTS location VARCHAR(150);
ALTER TABLE job_vacancies ADD COLUMN IF NOT EXISTS seniority_level VARCHAR(50);
ALTER TABLE job_vacancies ADD COLUMN IF NOT EXISTS min_salary INT;

UPDATE job_vacancies
SET company_name = COALESCE(company_name, 'Empresa não informada'),
    work_modality = COALESCE(work_modality, 'remoto'),
    employment_type = COALESCE(employment_type, 'clt'),
    seniority_level = COALESCE(seniority_level, 'pleno')
WHERE company_name IS NULL OR work_modality IS NULL OR employment_type IS NULL OR seniority_level IS NULL;

CREATE TABLE IF NOT EXISTS job_language_requirements (
    id BIGSERIAL PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES job_vacancies(id),
    language_name VARCHAR(50) NOT NULL,
    min_level VARCHAR(50) NOT NULL
);

UPDATE interview_invites
SET schedule_status = 'PROPOSED_BY_RECRUITER'
WHERE status = 'ACEITO'
  AND proposed_interview_at IS NOT NULL
  AND schedule_status IS NULL;

UPDATE interview_invites
SET schedule_status = 'AWAITING_SCHEDULE'
WHERE status = 'ACEITO'
  AND schedule_status IS NULL;

-- Região (5 valores) → Estado (UF). Limpa valores legados incompatíveis com o novo enum.
UPDATE anonymous_profiles
SET region_state = NULL
WHERE region_state IN ('NORTE', 'NORDESTE', 'CENTRO_OESTE', 'SUDESTE', 'SUL');

UPDATE job_vacancies
SET location = lower(location)
WHERE location IS NOT NULL
  AND length(trim(location)) = 2
  AND lower(location) ~ '^[a-z]{2}$';

UPDATE job_vacancies
SET location = NULL
WHERE location IS NOT NULL
  AND lower(location) NOT IN (
    'ac','al','ap','am','ba','ce','df','es','go','ma','mt','ms','mg',
    'pa','pb','pr','pe','pi','rj','rn','rs','ro','rr','sc','sp','se','to'
  );

ALTER TABLE anonymous_profiles ADD COLUMN IF NOT EXISTS salary_range VARCHAR(30);
ALTER TABLE job_vacancies ADD COLUMN IF NOT EXISTS salary_range VARCHAR(30);

CREATE TABLE IF NOT EXISTS profile_soft_skills (
    profile_id UUID NOT NULL REFERENCES anonymous_profiles(id),
    soft_skill VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS profile_desired_benefits (
    profile_id UUID NOT NULL REFERENCES anonymous_profiles(id),
    benefit VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS job_soft_skills (
    job_id UUID NOT NULL REFERENCES job_vacancies(id),
    soft_skill VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS job_benefits (
    job_id UUID NOT NULL REFERENCES job_vacancies(id),
    benefit VARCHAR(40) NOT NULL
);

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS idp_subject VARCHAR(128);
CREATE UNIQUE INDEX IF NOT EXISTS accounts_idp_subject_key ON accounts (idp_subject);
