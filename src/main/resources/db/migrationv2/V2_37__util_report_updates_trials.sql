
-- updating the main report function to include jurors on trials who do not have an appearance record
-- they should be counted as sitting and attended if they are on a trial and the report date is within the trial dates
CREATE OR REPLACE FUNCTION juror_mod.util_report_main(p_loc_code text, p_start_date date, p_end_date date)
 RETURNS TABLE(juror_number character varying, report_from date, report_to date, service_from date, service_to date, trial character varying, jury_from date, jury_to date, report_date date, within_service boolean, active_trial boolean, attendance boolean, holiday boolean, is_weekend boolean, working integer, sitting integer, attended integer)
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
	case when pool_members.within_service and panel_members.active_trial and (((appearance.attendance is null or appearance.attendance = false) and pool_members.holiday = false and juror_mod.util_is_weekend(pool_members.report_date) = false) or appearance.attendance) = true then 1 else 0 end as sitting,
	case when appearance.attendance or case when pool_members.within_service and panel_members.active_trial and (((appearance.attendance is null or appearance.attendance = false) and pool_members.holiday = false and juror_mod.util_is_weekend(pool_members.report_date) = false) or appearance.attendance) = true then true else false end then 1 else 0 end as attended

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
