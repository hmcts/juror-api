--DROP FUNCTION juror_mod.service_stats_main(boolean, date);

-- Created for JS-631
-- Database function to collate and return stats for SDP / PowerBI

CREATE OR REPLACE FUNCTION juror_mod.service_stats_main(p_exclude_redundant_courts boolean, p_from_date date)
RETURNS TABLE(
report_year text,
report_month text,
region_name character varying,
court_name character varying,
court_code character varying,
jurors_supplied_by_bureau integer,
utilisation_working_days integer,
utilisation_sitting_days integer,
utilisation_non_sitting_days integer,
utilisation_attendance_days integer,
utilisation_non_attendance_days integer,
service_length_number_of_days integer,
service_length_number_of_jurors integer)
LANGUAGE plpgsql
AS $function$

/* Parameters

   p_loc_codes_to_exclude	set to TRUE to exclude redundant courts
					        i.e. '407','428','464','750','754','756','758','761','774','794','795','797','798'

   p_from_date				date identifying the earliest month to be included in the result
*/

DECLARE

temprow RECORD;

BEGIN

-- create temp table to hold collated stats from each court
CREATE TEMP TABLE tmp_service_length_stats (
	service_year text NULL,
	service_month text NULL,
	court_code text NULL,
	number_of_service_days integer NULL,
	number_of_jurors integer NULL
) on COMMIT DROP;

-- Loop to collate the service stats for each court using the same util_report_main and util_report_appearance_list functions
-- that are used in the app to create the utilisation statistics reports.
FOR temprow IN

	SELECT cl.loc_code
	from   juror_mod.court_location cl
	WHERE  cl.loc_code not in ('000','400') -- always exclude Bureau owned jurors/pools, alweays exclude 000 which was created in error
	and    (p_exclude_redundant_courts is false
			or (p_exclude_redundant_courts is true and cl.loc_code not in ('407','428','464','750','754','756','758','761','774','794','795','797','798')))

LOOP

	insert into tmp_service_length_stats

		select to_char(service_date,'YYYY') as service_year,
			   to_char(service_date,'YYYY/MM') as service_month,
			   temprow.loc_code,
			   sum(number_working_days)::integer,
			   count(1)::integer as number_jurors
		from ( select pool_members.juror_number,
					  min(service_from) as service_date,
					  sum(working)::integer as number_working_days
			   from juror_mod.util_report_main( temprow.loc_code,
												date_trunc('MONTH',p_from_date - (3|| ' month')::interval)::date,
												current_date::date) as pool_members
                                                -- using 3 months before the from date inorder to identify and later exclude
												-- jurors that started their service before the from date
			   left join (select *
							from juror_mod.util_report_appearance_list( temprow.loc_code,
																		date_trunc('MONTH', p_from_date - (3|| ' month')::interval)::date,
																		current_date::date)) as appearance
					  on pool_members.juror_number = appearance.juror_number and pool_members.report_date = appearance.report_date
				group by pool_members.juror_number
				having min(service_from) between date_trunc('MONTH',p_from_date)::date and current_date::date
												 -- this is where the jurors that started their service before the from date are excluded
				) sd
		group by to_char(service_date,'YYYY'), to_char(service_date,'YYYY/MM'), temprow.loc_code;

END LOOP;

-- Collate number supplied by the Bureau, the utilisation stats created by the monthly utlisation report in the app
-- plus the service length stats gathered above, then return the results

return query

	select 	cal.report_year as report_year,
			cal.report_month as report_month,
			cal.region_name as region_name,
			cal.court_name as court_name,
			cal.court_code as court_code,
			coalesce(sbb.jurors_supplied, 0) as jurors_supplied_by_bureau,
			u.working_days::integer as utilisation_working_days,
			u.sitting_days::integer as utilisation_sitting_days,
			u.non_sitting_days::integer as utilisation_non_sitting_days,
			u.attendance_days::integer as utilisation_attendance_days,
			u.non_attendance_days::integer as utilisation_non_attendance_days,
			coalesce(sl.number_of_service_days,0)::integer as service_length_number_of_days,
			coalesce(sl.number_of_jurors,0)::integer as service_length_number_of_jurors
	from 	(select to_char(report_month.report_day,'YYYY') as report_year,
					to_char(report_month.report_day,'YYYY/MM') as report_month,
					cr.region_name as region_name,
					cl.loc_name as court_name,
					cl.loc_code as court_code
					from (select generate_series(date_trunc('MONTH',p_from_date)::date, current_date::date, '1 month'::interval)::date as report_day) as report_month
						  cross join juror_mod.court_location cl
						  join  juror_mod.court_region cr on cr.region_id = cl.region_id
						  where cl.loc_code not in ('000','400')
						  and   (p_exclude_redundant_courts is false
								 or (p_exclude_redundant_courts is true
									 and cl.loc_code not in ('407','428','464','750','754','756','758','761','774','794','795','797','798')))
					) cal -- List of report months/courts
	left join (select to_char(p.return_date,'YYYY') as report_year,
					  to_char(p.return_date,'YYYY/MM') as report_month,
					  substr(pool_number,1,3) as court_code,
					  count(1)::integer as jurors_supplied
				from juror_mod.bureau_snapshot bs, juror_mod.pool p
				where status = 2 and p.pool_no = bs.pool_number
				group by to_char(p.return_date,'YYYY'), to_char(p.return_date,'YYYY/MM'), substr(pool_number,1,3)) sbb -- Number Supplied by Bureau
			on  sbb.report_year = cal.report_year and sbb.report_month = cal.report_month and sbb.court_code = cal.court_code
	left join (select to_char(us.month_start,'YYYY') as report_year,
					  to_char(us.month_start,'YYYY/MM') as report_month,
					  us.loc_code as court_code,
					  us.available_days::integer working_days, -- WD
					  us.sitting_days::integer sitting_days, -- SD
					  us.attendance_days::integer - us.sitting_days non_sitting_days, -- AD-SD
					  us.attendance_days::integer attendance_days, -- AD
					  us.available_days::integer - us.attendance_days non_attendance_days -- WD-AD
					  -- N.B. utilisation ratio SD/WD has to be calculated when consolidating/summarising the data
				from  juror_mod.utilisation_stats us
				where us.month_start between date_trunc('MONTH',p_from_date)::date and current_date::date) u -- Utilisation
			on  u.report_year = cal.report_year and u.report_month = cal.report_month and u.court_code = cal.court_code
	left join tmp_service_length_stats sl
		   on sl.service_year = cal.report_year and sl.service_month = cal.report_month and sl.court_code = cal.court_code
	order by 1,2,3,4;

END;

$function$

;


