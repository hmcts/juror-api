alter table juror_mod.appearance 
alter column f_audit drop not null;

alter table juror_mod.appearance
alter column f_audit drop default;