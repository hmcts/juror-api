CREATE TABLE juror_mod.user_roles
(
    username varchar(20) NOT NULL,
    role     varchar(30) NOT NULL,

    CONSTRAINT user_roles_pkey PRIMARY KEY (username, role),
    CONSTRAINT user_roles_user_fkey FOREIGN KEY (username) REFERENCES juror_mod.users (username),
    CONSTRAINT user_roles_role_val CHECK (((role)::text = ANY
                                           ((ARRAY [
                                               'COURT_OFFICER'::character varying,
                                               'BUREAU_OFFICER'::character varying,
                                               'MANAGER'::character varying,
                                               'SENIOR_JUROR_OFFICER'::character varying,
                                               'ADMINISTRATOR'::character varying])
                                               ::text[])))

);

ALTER TABLE juror_mod.users
    ADD COLUMN user_type varchar(30) NULL;

ALTER TABLE juror_mod.users
    ADD CONSTRAINT user_type_val CHECK (((users.user_type)::text = ANY
                                         ((ARRAY [
                                             'COURT'::character varying,
                                             'BUREAU'::character varying,
                                             'ADMINISTRATOR'::character varying])
                                             ::text[])))
