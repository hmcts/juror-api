-- The following sql taken from v2_15__update_juror_add_response_entered_column.sql
-- and V2_13_summoning_updates.sql as it will take a long time to run and we want to run
-- it in a separate migration to avoid deployment issues.

alter table juror_mod.voters
    drop column postcode_start,
    add column postcode_start VARCHAR(10) generated always as (split_part(zip, ' ', 1)) stored;

CREATE INDEX voters_postcode_start_idx ON juror_mod.voters (postcode_start,loc_code,perm_disqual,flags,dob);

UPDATE juror_mod.juror j
SET response_entered = TRUE
FROM juror_mod.juror_pool jp
WHERE j.juror_number = jp.juror_number
  AND jp.status NOT IN (1, 9);