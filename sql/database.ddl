-- public.pr_commit definition

-- Drop table

-- DROP TABLE public.pr_commit;

CREATE TABLE public.pr_commit (
	sha varchar NOT NULL,
	login varchar NOT NULL,
	pull_request_id int4 NOT NULL,
	CONSTRAINT pr_commit_pk PRIMARY KEY (sha, pull_request_id)
);


-- public.pull_request definition

-- Drop table

-- DROP TABLE public.pull_request;

CREATE TABLE public.pull_request (
	project_repo varchar NOT NULL,
	login varchar NOT NULL,
	status varchar NOT NULL,
	project_user varchar NOT NULL,
	id int4 NOT NULL,
	CONSTRAINT pull_request_pk PRIMARY KEY (project_repo, status, project_user, id),
	CONSTRAINT pull_request_unique UNIQUE (project_repo, login, status, project_user, id)
);