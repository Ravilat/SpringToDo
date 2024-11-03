CREATE TABLE IF NOT EXISTS public.tasks
(
    id          bigserial             NOT NULL,
    title       character varying(50) NOT NULL,
    description character varying(500),
    status      character varying(30) NOT NULL,
    priority    int,
    created_at  date                  NOT NULL,
    due_date    date,
    CONSTRAINT check_status CHECK ( status IN ('NEW', 'IN_PROGRESS', 'COMPLETED', 'DELETED', 'CANCELLED')),
    CONSTRAINT check_priority CHECK ( priority between 1 AND 5),
    CONSTRAINT check_due CHECK ( due_date > created_at )
);