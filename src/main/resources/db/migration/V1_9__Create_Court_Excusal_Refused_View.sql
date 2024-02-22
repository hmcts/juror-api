create or replace view juror_mod.court_excusal_refused as
select		jp."owner", jp.pool_number, j.juror_number, j.first_name, j.last_name, j.postcode, js.status_desc,
            j.date_excused, d.description as excusal_reason, jh.date_created as date_printed, jp.is_active,
            row_number() over(partition by j.juror_number order by j.date_excused desc) as row_no
from 		juror_mod.juror_pool as jp
inner join	juror_mod.juror as j
	on		j.juror_number = jp.juror_number
inner join  juror_mod.t_exc_code d
    on      d.exc_code = j.excusal_code
inner join  juror_mod.t_juror_status js
    on      js.status = jp.status
inner join juror_mod.juror_history jh on
			jh.juror_number = j.juror_number
			and jh.history_code = 'PEXC'
			and lower(jh.other_information) like 'refuse excuse%'
			and jh.date_created > j.bureau_transfer_date
left join 	juror_mod.juror_history as jh_lett
	on		jh.juror_number = jp.juror_number
			and jh_lett.pool_number = jp.pool_number
			and jh_lett.history_code = 'REDL'
			and jh_lett.date_created = j.date_excused
			and jh_lett.date_created > j.bureau_transfer_date
where 		jp."owner" <> '400'
			and jp.is_active = true
			and j.acc_exc = 'Y';
		