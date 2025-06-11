

-- update the pending juror table
alter table juror_mod.pending_juror
ALTER COLUMN last_name TYPE VARCHAR(25);
