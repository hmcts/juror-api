CREATE TABLE juror_mod.users
(
    owner                 varchar(3)   NOT NULL,
    username              varchar(20)  NOT NULL,
    name                  varchar(50)  NOT NULL,
    level                 int2         NOT NULL,
    active               bool         NOT NULL DEFAULT true,
    last_logged_in        timestamp(6) NULL,
    version               int8         NULL,
    team_id               int8         NULL,

--  Temp fields until we migrate over to active directory
    password              varchar(20)  NULL,
    password_warning      boolean      NULL,
    days_to_expire        int8         NULL,
    password_changed_date timestamp(6) NULL,
    failed_login_attempts int2         NOT NULL DEFAULT 0,
    login_enabled_yn varchar(1) NULL DEFAULT 'Y'::character varying,
-- END
    CONSTRAINT users_pkey PRIMARY KEY (username)
);