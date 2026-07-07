-- update the juror table
alter table juror_mod.juror ADD date_summoned date;

alter table juror_mod.juror ADD hash_id bigint;

ALTER TABLE juror_mod.juror
ADD CONSTRAINT juror_date_summoned_hash_id_uk
UNIQUE (date_summoned, hash_id);

/* Roll back if required - this will drop the date_summoned and hash_id columns, and the unique constraint on those columns.
   ALTER TABLE juror_mod.juror
  DROP CONSTRAINT IF EXISTS juror_date_summoned_hash_id_uk;

  ALTER TABLE juror_mod.juror
  DROP COLUMN IF EXISTS hash_id;

  ALTER TABLE juror_mod.juror
  DROP COLUMN IF EXISTS date_summoned;

 */
