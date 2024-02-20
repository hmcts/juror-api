-- juror_mod.court_deferral_denied source
create or replace view juror_mod.court_deferral_denied as
select
	jp.owner,
	jp.pool_number,
	j.juror_number,
	j.first_name,
	j.last_name,
	j.postcode,
	js.status_desc,
	jh.date_created as refusal_date,
	jh.other_information,
	jh_lett.date_created as date_printed,
	jp.is_active,
	row_number() over (partition by j.juror_number
order by
	jh.date_created desc) as row_no
from
	juror_mod.juror_pool jp
join juror_mod.juror j on
	j.juror_number = jp.juror_number
join juror_mod.t_juror_status js on
	js.status = jp.status
join juror_mod.juror_history jh on
	jh.juror_number = j.juror_number
	and jh.history_code = 'PDEF'
	and lower(jh.other_information) like 'deferral denied%'
	and jh.date_created > j.bureau_transfer_date
left join juror_mod.juror_history jh_lett on
	jh_lett.juror_number = j.juror_number
	and jh_lett.history_code = 'RDDL'
	and jh_lett.date_created = jh.date_created
where
	upper(j.acc_exc) = 'Z'
	and jp.owner <> '400'
	and jp.is_active = true;