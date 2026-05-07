-- update the juror table
alter table juror_mod.juror ADD date_summoned date;

alter table juror_mod.juror ADD hash_id bigint;

ALTER TABLE juror_mod.juror
ADD CONSTRAINT juror_date_summoned_hash_id_uk
UNIQUE (date_summoned, hash_id);

