ALTER TABLE juror_mod.juror
    DROP COLUMN completion_flag;

ALTER TABLE juror_mod.juror_pool
    DROP COLUMN completion_flag;

ALTER TABLE juror_mod.juror_pool
    DROP COLUMN completion_date;