CREATE OR REPLACE FUNCTION juror_mod.get_active_pools_at_court_location(p_loccode text)
 RETURNS TABLE(pool_number character varying, total_possible_in_attendance bigint, in_attendance bigint, on_call bigint, total_possible_on_trial bigint, jurors_on_trial bigint, pool_type character varying, service_start_date date)
 LANGUAGE plpgsql
AS $function$
	begin

	-- find jurors on call and possible number of jurors on trial and attendance, limit the search to 4 weeks to improve performance
	return query with cte_first_query as (
		select p.pool_no as pool_number,
		sum(case when jp.status = 2 and jp.on_call = true then 1 else 0 end) as on_call,
		sum(case when jp.status = 2 and (jp.on_call is null or jp.on_call = false) then 1 else 0 end) as total_possible_in_attendance,
		sum(case when jp.status in (3,4) then 1 else 0 end) as total_possible_on_trial,
		p.pool_type,
		p.return_date as service_start_date
		from juror_mod.juror_pool jp
		join juror_mod.pool p
		on jp.pool_number = p.pool_no
		where p.loc_code  = p_loccode and p.return_date <= current_date and jp.is_active = true and p.return_date >= current_date - interval '4 weeks'
		group by pool_no
		),
		cte_second_query as (  -- find jurors who are physically at court today
		select p.pool_no,
		sum(case when jp.status = 2 and a.appearance_stage = 'CHECKED_IN' then 1 else 0 end) as in_attendance,
		sum(case when jp.status in (3,4) and a.appearance_stage = 'CHECKED_IN' then 1 else 0 end) as jurors_on_trial
		from juror_mod.juror_pool jp
		join juror_mod.pool p
		on jp.pool_number = p.pool_no
		left join juror_mod.appearance a
		on jp.juror_number = a.juror_number and jp.pool_number = a.pool_number
		where p.loc_code  = p_loccode and a.attendance_date = current_date and jp.is_active = true
		group by pool_no
		)

		select cte1.pool_number, cte1.total_possible_in_attendance, cte2.in_attendance, cte1.on_call, cte1.total_possible_on_trial,
		cte2.jurors_on_trial, cte1.pool_type, cte1.service_start_date
		from cte_first_query cte1
		join cte_second_query cte2
		on cte1.pool_number = cte2.pool_no;

	END;
$function$
;