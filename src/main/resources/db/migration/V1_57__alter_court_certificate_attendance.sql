-- juror_mod.court_certificate_attendance source
create or replace view juror_mod.court_certificate_attendance as
select
	jp.owner,
	jp.pool_number,
	j.juror_number,
	j.first_name,
	j.last_name,
	j.completion_date,
	p.return_date as start_date,
	jh.date_created as date_printed,
	row_number() over (partition by j.juror_number order by jh.date_created desc) as row_no,
	j.postcode,
	jp.is_active,
	jp.status as status_desc
from
	juror_mod.juror_pool jp
join juror_mod.juror j on j.juror_number = jp.juror_number
join juror_mod.pool p on jp.pool_number = p.pool_no
join juror_mod.t_juror_status js on js.status = jp.status
left join juror_mod.juror_history jh on
	jh.juror_number = j.juror_number
	and jh.history_code = 'RCER'
	and jh.date_created > j.bureau_transfer_date
where jp.owner <> '400'
	and jp.is_active = true
	and exists (
		select DISTINCT juror_number
		from juror_mod.appearance a
		where a.juror_number = jp.juror_number
		and a.loc_code = p.loc_code
		and a.attendance_type <> 'ABSENT' or a.attendance_type is null
		and a.no_show <> true or a.no_show is null)
