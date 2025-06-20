
-- update the coroner pool detail table to increase lastname length to 25 characters
alter table juror_mod.coroner_pool_detail
ALTER COLUMN last_name TYPE VARCHAR(25);
