ALTER TABLE juror_mod.juror
  ADD COLUMN response_entered BOOLEAN;

UPDATE juror_mod.juror j
SET response_entered = TRUE
FROM juror_mod.juror_pool jp
WHERE j.juror_number = jp.juror_number
  AND jp.status NOT IN (1, 9);
