
-- dropping the not null constraint on email as it may not exist in migrated data
alter table juror_mod.coroner_pool alter column email drop not null;