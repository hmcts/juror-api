create or replace view juror_mod.court_excusal_granted
as select jp.owner,
          jp.pool_number,
          j.juror_number,
          j.first_name,
          j.last_name,
          j.postcode,
          js.status_desc,
          j.date_excused,
          d.description as excusal_reason,
          jh.date_created as date_printed,
          jp.is_active,
          row_number() over(partition by j.juror_number order by j.date_excused desc) as row_no
 from juror_mod.juror_pool jp
   join juror_mod.juror j on j.juror_number = jp.juror_number
   join juror_mod.t_exc_code d on d.exc_code = j.excusal_code
   join juror_mod.t_juror_status js on js.status = jp.status
   left join juror_mod.juror_history jh ON jh.juror_number = jp.juror_number
   and jh.pool_number = jp.pool_number AND jh.history_code = 'REXC'
   and  jh.date_created::date > j.bureau_transfer_date::date
 where jp.status = 5 and jp.owner <> '400'