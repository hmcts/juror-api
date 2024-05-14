-- Function to return daily utilisation stats for a court between two dates
CREATE OR REPLACE FUNCTION juror_mod.util_daily_summary(p_loc_code text, p_start_date date, p_end_date date)
 RETURNS TABLE(
	report_date date,
	working bigint,
	sitting bigint,
	attended bigint,
	non_attendance bigint,
	utilisation numeric)
 LANGUAGE plpgsql
AS $function$
begin

return query select
	report_main.report_date,
	sum(report_main.working) as working_days,
	sum(report_main.sitting) as sitting_days,
	sum(report_main.attended) as attendance_days,
	sum(report_main.non_attendance) as non_attendance_days,
	case when sum(report_main.working) = 0 then 0 else sum(report_main.sitting)::decimal/sum(report_main.working) end as utilisation
	from (select * from juror_mod.util_report_main(p_loc_code, p_start_date, p_end_date)) as report_main
	group by report_main.report_date
	order by report_main.report_date;

END;
$function$
;

-- Function to return the main table for juror attendance
-- Working = Within service and ((not holiday and not weekend) or attended))
-- Sitting = Within service and Active trial and ((not attendance and not holiday and not weekend) or attended))
-- Attendance = Attended or sitting
-- Non attendance = Working - Attendance
CREATE OR REPLACE FUNCTION juror_mod.util_report_main(p_loc_code text, p_start_date date, p_end_date date)
 RETURNS TABLE(
 	juror_number character varying,
	report_from date,
	report_to date,
	service_from date,
	service_to date,
	trial character varying,
	jury_from date,
	jury_to date,
	report_date date,
	within_service boolean,
	active_trial boolean,
	attendance boolean,
	holiday boolean,
	is_weekend boolean,
	working integer,
	sitting integer,
	attended integer,
	non_attendance integer)
 LANGUAGE plpgsql
AS $function$
begin


return query select
	pool_members.juror_number,
	p_start_date as report_from,
	p_end_date as report_to,
 	pool_members.service_from,
 	pool_members.service_to,
 	panel_members.trial,
 	panel_members.jury_from,
	panel_members.jury_to,
 	pool_members.report_date,
	pool_members.within_service,
	panel_members.active_trial,
	appearance.attendance,
	pool_members.holiday,
	juror_mod.util_is_weekend(pool_members.report_date) as is_weekend,
	case when pool_members.within_service and ((pool_members.holiday = false and juror_mod.util_is_weekend(pool_members.report_date) = false) or appearance.attendance) = true then 1 else 0 end  as working,
	case when pool_members.within_service and panel_members.active_trial and ((appearance.attendance = false and pool_members.holiday = false and juror_mod.util_is_weekend(pool_members.report_date) = false) or appearance.attendance) = true then 1 else 0 end as sitting,
	case when appearance.attendance or case when pool_members.within_service and panel_members.active_trial and ((appearance.attendance = false and pool_members.holiday = false and juror_mod.util_is_weekend(pool_members.report_date) = false) or appearance.attendance) = true then true else false end then 1 else 0 end as attended,
	case when pool_members.within_service and ((pool_members.holiday = false and juror_mod.util_is_weekend(pool_members.report_date) = false) or appearance.attendance) = true then 1 else 0 end - case when appearance.attendance or case when pool_members.within_service and panel_members.active_trial and ((appearance.attendance = false and pool_members.holiday = false and juror_mod.util_is_weekend(pool_members.report_date) = false) or appearance.attendance) = true then true else false end then 1 else 0 end as non_attendance

	from
	(select * from juror_mod.util_report_juror_service_list(p_loc_code, p_start_date, p_end_date)) as pool_members
	left join (select * from juror_mod.util_trial_participation_list(p_loc_code, p_start_date, p_end_date)) as panel_members
	on pool_members.juror_number = panel_members.juror_number
	left join (select * from juror_mod.util_report_appearance_list(p_loc_code, p_start_date, p_end_date)) as appearance
	on pool_members.juror_number = appearance.juror_number
	group by pool_members.juror_number,
	pool_members.report_date,
	pool_members.service_from,
 	pool_members.service_to,
 	panel_members.trial,
 	panel_members.jury_from,
	panel_members.jury_to,
 	pool_members.report_date,
	pool_members.within_service,
	panel_members.active_trial,
	appearance.attendance,
	pool_members.holiday
	order by pool_members.juror_number, pool_members.report_date;

END;
$function$
;


-- function to return jurors and service within the report window (also indicating public holidays)
CREATE OR REPLACE FUNCTION juror_mod.util_report_juror_service_list(p_loc_code text, p_start_date date, p_end_date date)
 RETURNS TABLE(
 	juror_number character varying,
	report_from date,
	report_to date,
	service_from date,
	service_to date,
	trial character varying,
	jury_from date,
	jury_to date,
	report_date date,
	within_service boolean,
	active_trial boolean,
	attendance boolean,
	holiday boolean)
 LANGUAGE plpgsql
AS $function$
begin

return query select
	pool_members.juror_number,
	p_start_date as report_from,
	p_end_date as report_to,
 	pool_members.service_start_date as service_from,
 	pool_members.service_end_date as service_to,
 	NULL::character varying,
 	null::date,
 	null::date,
 	report_days.report_date,
	case when report_days.report_date between pool_members.service_start_date and pool_members.service_end_date then true else false end as within_service,
	false,
	false,
	report_days.holiday
	from (select * from juror_mod.util_report_report_days_list(p_loc_code, p_start_date, p_end_date)) as report_days
	left join (select * from juror_mod.util_report_pool_members_list(p_loc_code, p_start_date, p_end_date)) as pool_members
	on report_days.report_date between pool_members.service_start_date and pool_members.service_end_date
	group by  pool_members.juror_number, report_days.report_date, report_days.holiday, pool_members.service_start_date, pool_members.service_end_date
	order by pool_members.juror_number, report_date;
END;
$function$
;


-- function to get the list of jurors in a court within a given date range (used in function above)
CREATE OR REPLACE FUNCTION juror_mod.util_report_pool_members_list(p_loc_code text, p_start_date date, p_end_date date)
 RETURNS TABLE(juror_number character varying, attendance_date date, return_date date, service_start_date date, service_end_date date, min_status integer)
 LANGUAGE plpgsql
AS $function$
begin

return query select
	jp.juror_number,
    min(a.attendance_date) attendance_date,
    min(case when jp.is_active = true then p.return_date else to_date('01/12/2099', 'dd/mm/yyyy') end ) return_date,
    greatest(least(coalesce(min(a.attendance_date), min(case when jp.is_active = true then p.return_date else to_date('01/12/2099', 'dd/mm/yyyy') end )),
    		min(case when jp.is_active = true then p.return_date else to_date('01/12/2099','dd/mm/yyyy') end )), p_start_date) service_start,
    least(min(coalesce(case when jp.status = 10 then jp.transfer_date else date(j.completion_date) end, p_end_date)),p_end_date) service_end,
    min(status) min_status
    from juror_mod.juror_pool jp
    join juror_mod.juror j
    on jp.juror_number = j.juror_number
    join juror_mod.pool p
    on jp.pool_number = p.pool_no
    left join juror_mod.appearance a
    on jp.juror_number = a.juror_number
    where p.loc_code = a.loc_code
    and (((jp.status in (2,3,4,10) or (jp.status = 7 and a.attendance_date is not null)) and jp.is_active ='Y')
          or (jp.status = 8 and a.attendance_date is not null and j.completion_date is not null))
    and p.loc_code = p_loc_code
    and ((jp.status = 10 and jp.transfer_date >= p_start_date)
           or (jp.status <> 10 and (j.completion_date is null or j.completion_date >= p_start_date)))
    group by jp.juror_number
    having (least(coalesce(min(a.attendance_date), min(case when jp.is_active = true then p.return_date else to_date('01/12/2099', 'dd/mm/yyyy') end)),
               min(case when jp.is_active = true then p.return_date else to_date('01/12/2099', 'dd/mm/yyyy') end)) <= p_end_date);
END;
$function$
;


-- function to return list of appearance for jurors within report window
CREATE OR REPLACE FUNCTION juror_mod.util_report_appearance_list(p_loc_code text, p_start_date date, p_end_date date)
 RETURNS TABLE(
  	juror_number character varying,
	report_from date,
	report_to date,
	service_from date,
	service_to date,
	trial character varying,
	jury_from date,
	jury_to date,
	report_date date,
	within_service boolean,
	active_trial boolean,
	attendance boolean,
	holiday boolean)
 LANGUAGE plpgsql
AS $function$
begin

return query select
	a.juror_number,
	p_start_date as report_from,
	p_end_date as report_to,
	null::date,
	null::date,
	null::character varying,
	null::date,
	null::date,
	a.attendance_date,
	true,
	a.sat_on_jury,
	true,
	null::boolean
	from juror_mod.appearance a
	where a.attendance_date between p_start_date and p_end_date
	and (a.non_attendance is null or a.non_attendance = false)
	and (a.no_show is null or a.no_show = false)
	and a.loc_code= p_loc_code
	order by a.juror_number, a.attendance_date;

END;
$function$
;


-- function to return juror participation in a trial within the report window
CREATE OR REPLACE FUNCTION juror_mod.util_trial_participation_list(p_loc_code text, p_start_date date, p_end_date date)
 RETURNS TABLE(
   	juror_number character varying,
	report_from date,
	report_to date,
	service_from date,
	service_to date,
	trial character varying,
	jury_from date,
	jury_to date,
	report_date date,
	within_service boolean,
	active_trial boolean,
	attendance boolean,
	holiday boolean)
 LANGUAGE plpgsql
AS $function$
begin

return query select
	jt.juror_number,
	p_start_date as report_from,
	p_end_date as report_to,
		null::date,
	null::date,
	jt.trial_number,
	jt.empanelled_date as jury_from,
	coalesce (jt.return_date, p_end_date) as jury_to,
	report_dates.report_date,
	true,
	case when report_dates.report_date between jt.empanelled_date and coalesce (jt.return_date, p_end_date) then true else false end as active_date,
	null::boolean,
	null::boolean
	from juror_mod.juror_trial jt
	left join (select * from juror_mod.util_report_report_days_list(p_loc_code, p_start_date, p_end_date)) as report_dates
	on report_dates.report_date between jt.empanelled_date and coalesce (jt.return_date, p_end_date) -- if no return date then assume still on trial for now
	where jt.loc_code  = p_loc_code
	and trim(jt.result) in ('J','R')
	group by jt.juror_number, jt.trial_number, jt.empanelled_date, jt.return_date, report_dates.report_date
	order by jt.juror_number, report_dates.report_date;

	END;
$function$
;


-- utility function to return list of all days within the report window and holiday flag
CREATE OR REPLACE FUNCTION juror_mod.util_report_report_days_list(p_loc_code text, p_start_date date, p_end_date date)
 RETURNS TABLE(report_date date, holiday boolean)
 LANGUAGE plpgsql
AS $function$
begin

return query select date(report_day.report_day) as report_date, case when h.holiday is not null then true else false end as holiday
	from generate_series(p_start_date::date, p_end_date::date, '1 day'::interval) report_day
	left join juror_mod.holiday h
	on report_day.date = h.holiday
	where h.loc_code is null or h.loc_code = p_loc_code
	order by report_day.report_day;

END;
$function$
;


-- utility to indicate if date is weekend
CREATE OR REPLACE FUNCTION juror_mod.util_is_weekend(p_date date)
 RETURNS boolean
 LANGUAGE plpgsql
AS $function$
begin

 return case when extract(dow from p_date) in (0,6) then true else false end;

END;
$function$
;
