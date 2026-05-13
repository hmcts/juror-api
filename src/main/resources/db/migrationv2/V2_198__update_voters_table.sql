
-- JS-892 update voters table schema

-- need to drop the view juror_mod.loc_postcode_totals_view before we can make changes to the voters table,
-- as the view depends on the voters table, and it will be updated to use the old voters table
-- automatically when we rename the existing voters table to voters_old
DROP VIEW IF EXISTS juror_mod.loc_postcode_totals_view;

-- back up the existing voters table before making changes
ALTER TABLE juror_mod.voters RENAME TO voters_old;
ALTER INDEX IF EXISTS juror_mod.voters_pkey RENAME TO voters_old_pkey;
ALTER INDEX IF EXISTS juror_mod.voters_optimized_idx RENAME TO voters_old_optimized_idx;
ALTER INDEX IF EXISTS juror_mod.voters_postcode_start_idx RENAME TO voters_old_postcode_start_idx;

-- create the new voters table with the updated schema
CREATE TABLE juror_mod.voters (
  hash_id int8 NOT NULL,
  register_lett varchar(5) NULL,
  poll_number varchar(5) NULL,
  title varchar(10) NULL,
  lname varchar(25) NOT NULL,
  fname varchar(20) NOT NULL,
  dob date NULL,
  flags varchar(2) NULL,
  address varchar(35) NOT NULL,
  address2 varchar(35) NULL,
  address3 varchar(35) NULL,
  address4 varchar(35) NULL,
  address5 varchar(35) NULL,
  postcode varchar(10) NULL,
  date_selected1 date NULL,
  la_id int4 NULL,
  perm_disqual varchar(1) NULL,
  source_id varchar(1) NULL,
  postcode_start varchar(10) GENERATED ALWAYS AS (split_part(postcode::text, ' '::text, 1)) STORED,
  CONSTRAINT voters_pkey PRIMARY KEY (hash_id)
);

-- recreate the view that was dropped
CREATE OR REPLACE VIEW juror_mod.loc_postcode_totals_view
AS SELECT cca.loc_code,
      v.postcode_start,
      sum(
               CASE
                 WHEN v.date_selected1 IS NULL AND v.perm_disqual IS NULL THEN 1
                 ELSE 0
                 END) AS total,
      sum(
               CASE
                 WHEN v.date_selected1 IS NULL AND v.perm_disqual IS NULL AND v.flags IS NULL THEN 1
                 ELSE 0
                 END) AS total_cor
FROM juror_mod.voters v
      JOIN juror_mod.court_catchment_area cca ON v.postcode_start::text = cca.postcode::text
WHERE v.date_selected1 IS NULL AND v.perm_disqual IS NULL
GROUP BY cca.loc_code, v.postcode_start;

CREATE INDEX voters_optimized_idx ON juror_mod.voters USING btree (postcode_start, date_selected1, perm_disqual, flags);
CREATE INDEX voters_postcode_start_idx ON juror_mod.voters USING btree (postcode_start, perm_disqual, flags, dob);

/* Roll back if required - this will drop the new voters table and recreate the old voters table with the original schema,
   and rename the indexes back to their original names. It will also drop and recreate the view that was dropped at the start of the migration.

DROP VIEW IF EXISTS juror_mod.loc_postcode_totals_view;

DROP TABLE IF EXISTS juror_mod.voters;

ALTER TABLE juror_mod.voters_old RENAME TO voters;

ALTER INDEX IF EXISTS juror_mod.voters_old_pkey RENAME TO voters_pkey;
ALTER INDEX IF EXISTS juror_mod.voters_old_optimized_idx RENAME TO voters_optimized_idx;
ALTER INDEX IF EXISTS juror_mod.voters_old_postcode_start_idx RENAME TO voters_postcode_start_idx;

 -- recreate the view that was dropped
CREATE OR REPLACE VIEW juror_mod.loc_postcode_totals_view
AS SELECT cca.loc_code,
      v.postcode_start,
      sum(
               CASE
                 WHEN v.date_selected1 IS NULL AND v.perm_disqual IS NULL THEN 1
                 ELSE 0
                 END) AS total,
      sum(
               CASE
                 WHEN v.date_selected1 IS NULL AND v.perm_disqual IS NULL AND v.flags IS NULL THEN 1
                 ELSE 0
                 END) AS total_cor
FROM juror_mod.voters v
      JOIN juror_mod.court_catchment_area cca ON v.postcode_start::text = cca.postcode::text
WHERE v.date_selected1 IS NULL AND v.perm_disqual IS NULL
GROUP BY cca.loc_code, v.postcode_start;
 */
