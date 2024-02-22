create or   replace view juror_mod.court_postponement as
select		jp."owner", jp.pool_number, j.juror_number, j.first_name, j.last_name, j.postcode, js.status_desc,
            jp.def_date, ec.description as deferral_reason, jh.date_created as date_printed, jp.is_active,
            row_number() over(partition by j.juror_number order by jp.def_date desc) as row_no
from 		juror_mod.juror_pool as jp
inner join	juror_mod.juror as j on	j.juror_number = jp.juror_number
inner join  juror_mod.t_exc_code as ec on ec.exc_code = jp.deferral_code
inner join  juror_mod.t_juror_status js on js.status = jp.status
left join 	juror_mod.juror_history as jh on jh.juror_number = jp.juror_number
			and jh.pool_number = jp.pool_number
			and jh.history_code = 'RPST'
			and jh.other_info_date = jp.def_date
			and jh.date_created > j.bureau_transfer_date
where 		jp.status = 7
and         ec.exc_code = 'P'
and         jp.owner <> '400'