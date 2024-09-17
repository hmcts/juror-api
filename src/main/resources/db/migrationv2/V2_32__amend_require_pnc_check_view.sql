-- juror_mod.require_pnc_check_view source

CREATE OR REPLACE VIEW juror_mod.require_pnc_check_view
AS SELECT j.police_check,
          j.juror_number,
          regexp_replace(j.first_name::text, '\s.*'::text, ''::text) AS first_name,
          NULLIF(regexp_replace(j.first_name::text, '.*?\s'::text, ''::text), j.first_name::text) AS middle_name,
          regexp_replace(j.last_name::text, '\s.*'::text, ''::text) AS last_name,
          j.dob AS date_of_birth,
          upper(regexp_replace(j.postcode::text, '\s'::text, ''::text)) AS post_code
   FROM juror_mod.juror j
            JOIN juror_mod.juror_pool jp ON jp.juror_number::text = j.juror_number::text
   WHERE jp.status = 2 AND (j.police_check IS NULL OR (j.police_check::text <> ALL (ARRAY['UNCHECKED_MAX_RETRIES_EXCEEDED'::character varying::text, 'ELIGIBLE'::character varying::text, 'INELIGIBLE'::character varying::text]))) AND jp.owner::text = '400'::text AND jp.is_active = true;
