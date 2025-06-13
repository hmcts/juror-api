
drop view juror_mod.loc_postcode_totals_view;

-- Update juror_mod.loc_postcode_totals_view source for performance (JS-410)
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

-- Covers both filtering and output
 CREATE INDEX voters_optimized_idx ON juror_mod.voters USING btree (postcode_start, date_selected1, perm_disqual, flags);
