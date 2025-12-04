
CREATE TABLE public.tasks
(
    id          bigserial             NOT NULL,
    title       character varying(50),
    description character varying(500),
    status      character varying(30) NOT NULL,
    priority    int,
    created_at  date                  NOT NULL,
    due_date    date
);