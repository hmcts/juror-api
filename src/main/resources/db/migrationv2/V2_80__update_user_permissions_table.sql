
alter table juror_mod.user_permissions drop constraint user_permissions_permission_val;

alter table juror_mod.user_permissions add constraint user_permissions_permission_val CHECK (((permission) = ANY (ARRAY['CREATE_JUROR', 'SUPER_USER'])))
