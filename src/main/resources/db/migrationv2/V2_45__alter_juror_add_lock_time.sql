-- This SQL script alters the 'juror' table in the 'juror_mod' schema.
-- It adds a new column named 'lock_time' of type DATE.

ALTER TABLE juror_mod.juror
  ADD COLUMN lock_time TIMESTAMP(0) NULL;
