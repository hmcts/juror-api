-- alter the Pool table

ALTER TABLE juror_mod.pool
ADD date_created TIMESTAMP,
ALTER COLUMN return_date type date;
