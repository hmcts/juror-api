update juror_mod.juror_pool jp
set on_call = false
where jp.on_call is null;

alter table juror_mod.juror_pool
    ALTER COLUMN on_call SET DEFAULT false,
    ALTER COLUMN on_call SET NOT NULL;