alter table juror_mod.users
     alter column username type varchar(30);

alter table juror_mod.users_audit
    alter column username type varchar(30);

alter table juror_mod.user_roles
    alter column username type varchar(30);

alter table juror_mod.user_roles_audit
    alter column username type varchar(30);

alter table juror_mod.juror_response
    alter column staff_login type varchar(30);

alter table juror_mod.juror_response_aud
    alter column login type varchar(30);

alter table juror_mod.juror_history
    alter column user_id type varchar(30);


alter table juror_mod.juror_pool
    alter column user_edtq type varchar(30);

alter table juror_mod.juror
    alter column user_edtq type varchar(30);

alter table juror_mod.financial_audit_details
    alter column created_by type varchar(30);

alter table juror_mod.message
    alter column username type varchar(30);

alter table juror_mod.contact_log
    alter column user_id type varchar(30);

alter table juror_mod.user_juror_response_audit
    alter column assigned_by type varchar(30),
    alter column assigned_to type varchar(30);

alter table juror_mod.pool_history
    alter column user_id type varchar(30);

alter table juror_mod.pool_history
    alter column user_id type varchar(30);