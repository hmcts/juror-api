-- update the juror table
alter table juror_mod.juror ADD date_summoned date;

alter table juror_mod.juror ADD hash_id bigint;

ALTER TABLE juror_mod.juror
ADD CONSTRAINT juror_date_summoned_hash_id_uk
UNIQUE (date_summoned, hash_id);

-- the following is to test setup before next ER load, which will create and populate hash_id field
--alter table juror_mod.voters add hash_id bigint;
--
--UPDATE juror_mod.voters
--SET hash_id = abs(hashtext(
--    coalesce(part_no,'') ||
--    coalesce(lname,'') ||
--    coalesce(zip,'')
--))::bigint;
