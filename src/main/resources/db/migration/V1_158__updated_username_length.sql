alter table juror_mod.users
    alter column created_by type varchar(30),
    alter column updated_by type varchar(30);

alter table juror_mod.users_audit
    alter column created_by type varchar(30),
    alter column updated_by type varchar(30);


alter table juror_mod.system_parameter
    alter column created_by type varchar(30),
    alter column updated_by type varchar(30);

alter table juror_mod.appearance
    alter column created_by type varchar(30),
    alter column last_updated_by type varchar(30);

alter table juror_mod.appearance_audit
    alter column created_by type varchar(30),
    alter column last_updated_by type varchar(30);


