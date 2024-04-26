update juror_mod.user_roles
    set role = 'MANAGER'
where role = 'TEAM_LEADER' and
    user_roles.username not in (select username from juror_mod.user_roles where role = 'MANAGER');

ALTER TABLE juror_mod.user_roles
    DROP CONSTRAINT user_roles_role_val,
    ADD CONSTRAINT user_roles_role_val CHECK (((role)::text = ANY
                                               ((ARRAY [
                                                   'MANAGER'::character varying,
                                                   'SENIOR_JUROR_OFFICER'::character varying
                                                   ])::text[])));