CREATE OR REPLACE VIEW juror_mod.show_cause
AS SELECT jp.owner,
     jp.pool_number,
     j.juror_number,
     j.first_name,
     j.last_name,
     j.postcode,
     js.status_desc,
     j.date_disq,
     jh.date_created AS date_printed,
     jp.is_active,
     a.attendance_date,
     row_number() over(partition by j.juror_number order by j.date_disq desc) as row_no
FROM juror_mod.juror_pool jp
JOIN juror_mod.juror j ON j.juror_number = jp.juror_number
JOIN juror_mod.t_juror_status js ON js.status = jp.status
JOIN juror_mod.appearance a ON a.juror_number = jp.juror_number
LEFT JOIN juror_mod.juror_history jh ON jh.juror_number = jp.juror_number
     AND jh.pool_number = jp.pool_number
     AND jh.history_code = 'RFTA'
     AND  jh.date_created::date > j.bureau_transfer_date::date
WHERE a.no_show = true
AND jp.owner <> '400'