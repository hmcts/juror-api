
-- This function is used to generate a list of jurors who are in a pool during a given date range.
-- Updated the query to ignore appearances for deferred jurors before the reporting period.
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
                     -- figure out if an appearance occurred before service start date and use that if applicable
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
                   and (((jp.status in (2,3,4,10,13) or (jp.status = 7 and a.attendance_date is not null and a.attendance_date between p_start_date and p_end_date)) and jp.is_active = true)
                     or (jp.status = 8 and a.attendance_date is not null and j.completion_date is not null))
                   and (a.non_attendance is null or a.non_attendance = false) and (a.no_show is null or a.no_show = false)
                   and p.loc_code = p_loc_code
                   and ((jp.status = 10 and jp.transfer_date >= p_start_date)
                     or (jp.status <> 10 and (j.completion_date is null or j.completion_date >= p_start_date)))
                 group by jp.juror_number
                 having (least(coalesce(min(a.attendance_date), min(case when jp.is_active = true then p.return_date else to_date('01/12/2099', 'dd/mm/yyyy') end)),
                               min(case when jp.is_active = true then p.return_date else to_date('01/12/2099', 'dd/mm/yyyy') end)) <= p_end_date);
END;
$function$
;