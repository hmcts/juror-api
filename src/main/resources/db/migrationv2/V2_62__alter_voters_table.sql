-- alter the voters table to remove the loc_code column and update the primary key
-- update indexes to remove the loc_code column
-- update the loc_postcode_totals_view to remove the loc_code column reference and use the court catchment area table to get the loc_code


-- update primary key
alter table juror_mod.voters
drop constraint voters_pk;
alter table juror_mod.voters
ADD CONSTRAINT voters_pk PRIMARY KEY (part_no);

-- loc code column will be dropped from the voters table
drop index voters_loc_code_idx;

-- removing the loc_code column
drop index voters_postcode_start_idx;
CREATE INDEX voters_postcode_start_idx ON juror_mod.voters USING btree (postcode_start, perm_disqual, flags, dob);

-- juror_mod.loc_postcode_totals_view source, remove the loc_code column reference and use the court catchment area table to get the loc_code
drop view juror_mod.loc_postcode_totals_view;

CREATE OR REPLACE VIEW juror_mod.loc_postcode_totals_view
AS SELECT cca.loc_code,
    v.zip,
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
   join juror_mod.court_catchment_area cca on v.postcode_start = cca.postcode
  WHERE v.date_selected1 IS NULL AND v.perm_disqual IS NULL
  GROUP BY cca.loc_code, v.zip;


-- drop the loc code column on voters table
alter table juror_mod.voters
drop column loc_code;

