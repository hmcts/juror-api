
-- utility function to return list of all days within the report window and holiday flag
-- updated to correctly handle no trial days added by a court and multiple holidays on the same day
CREATE OR REPLACE FUNCTION juror_mod.util_report_report_days_list(p_loc_code text, p_start_date date, p_end_date date)
 RETURNS TABLE(report_date date, holiday boolean)
 LANGUAGE plpgsql
AS $function$
begin

return query

with report_days as (
select generate_series(p_start_date, p_end_date, '1 day'::interval)::date as report_day)

select distinct(report_days.report_day) as report_date,
	case when h.holiday is not null then true else false end as holiday
from report_days
	left join juror_mod.holiday h on (report_days.report_day = h.holiday and (h.loc_code is null or h.loc_code = p_loc_code))
order by report_days.report_day;

	END;
$function$
;