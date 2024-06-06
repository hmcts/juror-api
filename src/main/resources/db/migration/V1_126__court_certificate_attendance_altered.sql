create or replace view juror_mod.court_certificate_attendance as
SELECT jp.owner,
    jp.pool_number,
    j.juror_number,
    j.first_name,
    j.last_name,
    j.completion_date,
    p.return_date AS start_date,
    jh.date_created AS date_printed,
    row_number() OVER (PARTITION BY j.juror_number ORDER BY jh.date_created DESC) AS row_no,
    j.postcode,
    jp.is_active,
    jp.status AS status_desc
   FROM juror_mod.juror_pool jp
     JOIN juror_mod.juror j ON j.juror_number::text = jp.juror_number::text
     JOIN juror_mod.pool p ON jp.pool_number::text = p.pool_no::text
     JOIN juror_mod.t_juror_status js ON js.status = jp.status
     LEFT JOIN juror_mod.juror_history jh ON jh.juror_number::text = j.juror_number::text AND jh.history_code::text = 'RCER'::text AND jh.date_created > j.bureau_transfer_date
  WHERE jp.owner::text <> '400'::text AND jp.is_active = true AND (EXISTS ( SELECT DISTINCT a.juror_number
           FROM juror_mod.appearance a
          where
          		a.juror_number::text = jp.juror_number::text
          		and a.attendance_type::text <> 'ABSENT'::text
          		and (a.no_show <> true
          		or a.no_show is null)));
