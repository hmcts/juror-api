CREATE OR REPLACE VIEW juror_mod.bulk_print_data_notify_comms
AS SELECT b.id,
    b.creation_date,
    b.form_type,
    b.detail_rec,
    b.extracted_flag,
    b.juror_no,
    b.digital_comms,
    n.template_id,
    n.template_name,
    n.notify_name,
    j.h_email,
    p.loc_code
FROM juror_mod.bulk_print_data b,
    juror_mod.notify_template_mapping n,
    juror_mod.juror j,
    juror_mod.juror_pool jp,
    juror_mod.pool p
WHERE
  b.form_type::text = n.form_type::text
AND
 b.juror_no::text = j.juror_number::text
AND
 jp.juror_number::text = j.juror_number::text
AND
 jp.pool_number::text = p.pool_no::text
AND
 b.creation_date > (CURRENT_DATE - 3)
AND
 b.digital_comms = false AND n.notification_type = 1
AND
 jp.owner::text = '400'::text
AND
 jp.is_active = true
AND
 j.h_email IS NOT NULL AND length(TRIM(BOTH FROM j.h_email)) > 0;
