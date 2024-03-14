ALTER TABLE juror_mod.users
--     DROP COLUMN owner,
    DROP COLUMN level,
    DROP COLUMN password,
    DROP COLUMN password_warning,
    DROP COLUMN password_changed_date,
    DROP COLUMN days_to_expire,
    DROP COLUMN failed_login_attempts,
    DROP COLUMN login_enabled_yn,
    DROP COLUMN can_approve,
    ALTER COLUMN owner DROP NOT NULL,
    ADD COLUMN "email" varchar(200) NOT NULL,
    ADD CONSTRAINT user_email_unique UNIQUE (email),
    DROP CONSTRAINT user_type_val,

    ADD CONSTRAINT user_type_val CHECK (((user_type)::text = ANY
                                         (ARRAY [
                                             ('SYSTEM'::character varying)::text,
                                             ('COURT'::character varying)::text,
                                             ('BUREAU'::character varying)::text,
                                             ('ADMINISTRATOR'::character varying)::text
                                             ])));

ALTER TABLE juror_mod.user_roles
    DROP CONSTRAINT user_roles_role_val,
    ADD CONSTRAINT user_roles_role_val CHECK (((role)::text = ANY
                                               ((ARRAY [
                                                   'MANAGER'::character varying,
                                                   'SENIOR_JUROR_OFFICER'::character varying,
                                                   'TEAM_LEADER'::character varying
                                                   ])::text[])));

CREATE TABLE juror_mod.user_courts
(
    username varchar(36) NOT NULL,
    loc_code varchar(3)  NOT NULL,

    CONSTRAINT user_courts_pkey PRIMARY KEY (username, loc_code),
    CONSTRAINT user_courts_user_fkey FOREIGN KEY (username) REFERENCES juror_mod.users (username),
    CONSTRAINT user_courts_court_val FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location (loc_code)
);
