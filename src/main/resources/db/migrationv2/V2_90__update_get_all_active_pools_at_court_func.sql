-- DROP FUNCTION juror_mod.get_active_pools_at_court_location(text);
-- JS-647 add criteria exclude bureau owned jurors in pools owned by courts != 400

CREATE OR REPLACE FUNCTION juror_mod.get_all_active_pools_at_court_location(p_loccode text)
 RETURNS TABLE(pool_number character varying, total_possible_in_attendance bigint, in_attendance bigint, on_call bigint, total_possible_on_trial bigint, jurors_on_trial bigint, pool_type character varying, service_start_date date)
 LANGUAGE plpgsql
AS $function$
begin

    -- find jurors on call and possible number of jurors on trial and attendance, limit the search to 4 weeks to improve performance
    return query with cte_first_query as (
                                  select
                                  	p.pool_no as pool_number,
                                  	p.loc_code as loc_code,
                                  	sum(case when jp.status = 2 and jp.on_call = true then 1 else 0 end) as on_call,
                                  	sum(case when jp.status = 2 and (jp.on_call is null or jp.on_call = false) then 1 else 0 end) as total_possible_in_attendance, /* should this say possibly instead of possible? */
                                  	sum(case when jp.status in (3, 4) then 1 else 0 end) as total_possible_on_trial, /* should this say possibly instead of possible? */
                                  	p.pool_type,
                                  	p.return_date as service_start_date
                                  from
                                  	juror_mod.juror_pool jp
                                  join juror_mod.pool p
                                  on
                                  	jp.pool_number = p.pool_no
                                  where
                                  	p.loc_code = p_loccode
                                  	and p.owner != '400'
                                  	and jp.owner != '400'
                                  	and jp.is_active = true
                                  	and p.return_date >= current_date - interval '4 weeks'
                                  group by
                                  	pool_no
                                      ),
                                        cte_second_query as (

                                  -- find jurors who are physically at court today
                                  select
                                  	p.pool_number as pool_no,
                                  	sum(case when jp.status = 2 and a.appearance_stage = 'CHECKED_IN' then 1 else 0 end) as in_attendance,
                                  	sum(case when jp.status in (3, 4) and jp.juror_number in (select juror_number from juror_mod.juror_trial jt where jt.result = 'J' or jt.result is null and jt.loc_code = p.loc_code) then 1 else 0 end) as jurors_on_trial
                                  from
                                  	cte_first_query p
                                  join juror_mod.juror_pool jp
                                  on
                                  	jp.pool_number = p.pool_number
                                  left join juror_mod.appearance a
                                  on
                                  	jp.juror_number = a.juror_number
                                  	and jp.pool_number = a.pool_number /* might not have pool_number for migrated data - loc_code would be better here*/
                                  where
                                  	a.attendance_date = current_date
                                  	and jp.is_active = true
                                  	and jp.owner != '400'
                                  	and jp.status in (2,3,4)
                                  group by
                                  	p.pool_number
                                        )

                                  select
                                  	cte1.pool_number,
                                  	cte1.total_possible_in_attendance,
                                  	cte2.in_attendance,
                                  	cte1.on_call,
                                  	cte1.total_possible_on_trial,
                                  	cte2.jurors_on_trial,
                                  	cte1.pool_type,
                                  	cte1.service_start_date
                                  from
                                  	cte_first_query cte1
                                  left join cte_second_query cte2
                                                 on
                                  	cte1.pool_number = cte2.pool_no;

END;
$function$
;
