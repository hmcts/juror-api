-- Create a View of the mod.bulk_print_data tables
DROP VIEW juror_mod.bulk_print_data_notify_comms;
create or replace  view  juror_mod.bulk_print_data_notify_comms ("id", "creation_date","form_type","detail_rec", "extracted_flag","juror_no","digital_comms", "template_id", "template_name", "notify_name", "h_email" ) AS
select

    b.id,
    b.creation_date,
    b.form_type,
    b.detail_rec,
    b.extracted_flag,
    b.juror_no,
    b.digital_comms,
    n.template_id,
    n.template_name,
    n.notify_name,
    j.h_email
FROM
    juror_mod.bulk_print_data b,
    "juror_mod"."notify_template_mapping" n,
    "juror_mod"."juror" j,
    "juror_mod"."juror_pool" jp
WHERE
    b.form_type = n.form_type
  and  b.juror_no = j.juror_number
  and jp.juror_number = j.juror_number
  and  b.creation_date > (current_date - 3)
  and  b.digital_comms = 'N'
  and  n.notification_type = 1
  and  jp.owner = '400'
  and jp.is_active = 'Y'
  and trim(J.h_email) is not null;

