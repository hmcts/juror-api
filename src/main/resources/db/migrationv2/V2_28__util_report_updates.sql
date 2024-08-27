-- updated the trial participation list to remove duplicate juror entries for the same report date
drop function if exists juror_mod.util_trial_participation_list;

-- function to return juror participation in a trial within the report window, updated to return row number for deduplication
-- only want to count one trial per juror per report date
CREATE OR REPLACE FUNCTION juror_mod.util_trial_participation_list(p_loc_code text, p_start_date date, p_end_date date)
 RETURNS TABLE(juror_number character varying,
 report_from date,
 report_to date,
 trial character varying,
 jury_from date,
 jury_to date,
 report_date date,
 within_service boolean,
 active_trial boolean,
 row_no bigint)
 LANGUAGE plpgsql
AS $function$
begin

return query select
	jt.juror_number,
	p_start_date as report_from,
	p_end_date as report_to,
	jt.trial_number,
	jt.empanelled_date as jury_from,
	coalesce (jt.return_date, p_end_date) as jury_to, -- if no return date then assume still on trial for now
	report_dates.report_date,
	true, -- defaulting within service to true if on a trial
	case when report_dates.report_date between jt.empanelled_date and coalesce (jt.return_date, p_end_date) then true else false end as active_date,
	row_number() OVER (PARTITION BY jt.juror_number,report_dates.report_date) AS row_no
	from juror_mod.juror_trial jt
	join (select * from juror_mod.util_report_report_days_list(p_loc_code, p_start_date, p_end_date)) as report_dates
	on report_dates.report_date between jt.empanelled_date and coalesce (jt.return_date, p_end_date) -- if no return date then assume still on trial for now
	where jt.loc_code  = p_loc_code
	and trim(jt.result) in ('J','R')
	and jt.empanelled_date <= p_end_date
	and (jt.return_date is null or (jt.return_date >= p_start_date))
	group by jt.juror_number, jt.trial_number, jt.empanelled_date, jt.return_date, report_dates.report_date
	order by jt.juror_number, report_dates.report_date;

	END;
$function$
;

-- function to return the de-duplicated trial participation list where row number = 1
CREATE OR REPLACE FUNCTION juror_mod.util_trial_participation_list_dedup(p_loc_code text, p_start_date date, p_end_date date)
 RETURNS TABLE(juror_number character varying,
 report_from date,
 report_to date,
 trial character varying,
 jury_from date,
 jury_to date,
 report_date date,
 within_service boolean,
 active_trial boolean,
 row_no bigint)
 LANGUAGE plpgsql
AS $function$
begin

return query
		select tp.juror_number, tp.report_from, tp.report_to, tp.trial, tp.jury_from, tp.jury_to, tp.report_date, tp.within_service, tp.active_trial, tp.row_no
		from juror_mod.util_trial_participation_list(p_loc_code, p_start_date, p_end_date) tp
		where tp.row_no = 1;
	END;
$function$
;

-- updated to invoke the deduplication trial participation function
CREATE OR REPLACE FUNCTION juror_mod.util_report_main(p_loc_code text, p_start_date date, p_end_date date)
 RETURNS TABLE(juror_number character varying,
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
 attended integer)
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
	case when appearance.attendance or case when pool_members.within_service and panel_members.active_trial and ((appearance.attendance = false and pool_members.holiday = false and juror_mod.util_is_weekend(pool_members.report_date) = false) or appearance.attendance) = true then true else false end then 1 else 0 end as attended

	from
	(select * from juror_mod.util_report_juror_service_list(p_loc_code, p_start_date, p_end_date)) as pool_members
	left join (select * from juror_mod.util_trial_participation_list_dedup(p_loc_code, p_start_date, p_end_date)) as panel_members
	on pool_members.juror_number = panel_members.juror_number and pool_members.report_date = panel_members.report_date
	left join (select * from juror_mod.util_report_appearance_list(p_loc_code, p_start_date, p_end_date)) as appearance
	on pool_members.juror_number = appearance.juror_number and pool_members.report_date = appearance.report_date
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