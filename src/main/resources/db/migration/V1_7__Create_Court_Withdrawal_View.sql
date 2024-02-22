-- juror_mod.court_withdrawal source
CREATE OR REPLACE VIEW juror_mod.court_withdrawal
AS SELECT jp.owner,
    jp.pool_number,
    j.juror_number,
    j.first_name,
    j.last_name,
    j.postcode,
    js.status_desc,
    j.date_disq,
    d.disq_code AS disq_code,
    jh.date_created AS date_printed,
    jp.is_active,
    row_number() over(partition by j.juror_number order by j.date_disq desc) as row_no
   FROM juror_mod.juror_pool jp
     JOIN juror_mod.juror j ON j.juror_number = jp.juror_number
     JOIN juror_mod.t_disq_code d ON d.disq_code = j.disq_code
     JOIN juror_mod.t_juror_status js ON js.status = jp.status
     LEFT JOIN juror_mod.juror_history jh ON jh.juror_number = jp.juror_number
     AND jh.pool_number = jp.pool_number AND jh.history_code = 'RDIS'
     and  jh.date_created::date > j.bureau_transfer_date::date
	 where jp.status = 6 and jp.owner <> '400'