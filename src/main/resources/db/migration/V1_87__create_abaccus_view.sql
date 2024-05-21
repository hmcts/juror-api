-- juror_digital.abaccus source

CREATE OR REPLACE VIEW juror_mod.abaccus
AS SELECT pf.form_type,
    to_date(pf.creation_date::text, 'YYYY-MM-DD'::text) AS creation_date,
    count(pf.part_no) AS number_of_items
   FROM juror.print_files pf
  GROUP BY pf.form_type, (to_date(pf.creation_date::text, 'YYYY-MM-DD'::text));