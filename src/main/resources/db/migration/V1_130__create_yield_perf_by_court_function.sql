
-- Function to return yield performance report
create or replace function juror_mod.yield_performance_report(p_loc_code_list varchar, p_start_date date, p_end_date date)
 returns table(
 	loc_code varchar,
 	court_name varchar,
 	no_requested integer,
	supplied integer
)

 language plpgsql
AS $function$
begin

return query

with current_data as (select p.loc_code,
0::integer as no_requested,
sum(case when coalesce(jp.status,0) = 2 then 1 else 0 end) as supplied
from juror_mod.juror_pool jp
join juror_mod.pool p on jp.pool_number = p.pool_no and p.loc_code in (select unnest(string_to_array(p_loc_code_list, ','))) and p.return_date between p_start_date and p_end_date
join juror_mod.juror j on jp.juror_number = j.juror_number
left join juror_mod.juror_response jr on jp.juror_number = jr.juror_number
where jp.owner = '400'
group by p.loc_code),


snapshot_data as (select p.loc_code,
0::integer as no_requested,
sum(case when coalesce(bs.status,0) = 2 then 1 else 0 end) as supplied
from juror_mod.bureau_snapshot bs
join juror_mod.pool p on bs.pool_number = p.pool_no and p.loc_code in (select unnest(string_to_array(p_loc_code_list, ','))) and p.return_date between p_start_date and p_end_date
and p."owner" <> '400'
group by p.loc_code),


no_requested_with_bureau as (select p.loc_code,
p.no_requested as no_requested,
0::integer as supplied
from juror_mod.pool p
where p.loc_code in (select unnest(string_to_array(p_loc_code_list, ','))) and p.return_date between p_start_date and p_end_date
and p."owner" = '400'
group by p.loc_code, p.no_requested),


no_requested_from_snapshot as (select p.loc_code,
p.no_requested as no_requested
from juror_mod.bureau_snapshot bs
join juror_mod.pool p on bs.pool_number = p.pool_no and p.loc_code in (select unnest(string_to_array(p_loc_code_list, ','))) and p.return_date between p_start_date and p_end_date
and p."owner" <> '400'
group by p.loc_code, p.no_requested),


sum_snaphsot_requested as (select nrs.loc_code,
sum(nrs.no_requested) as no_requested,
0::integer as supplied
from no_requested_from_snapshot nrs
group by nrs.loc_code),


union_tables as (select cd.loc_code as loc_code, cd.no_requested as no_requested, cd.supplied as supplied from current_data cd
union all select sd.loc_code as loc_code, sd.no_requested as no_requested, sd.supplied as supplied from snapshot_data sd
union all select nrb.loc_code as loc_code, nrb.no_requested as no_requested, nrb.supplied as supplied from no_requested_with_bureau nrb
union all select snr.loc_code as loc_code, snr.no_requested as no_requested, snr.supplied as supplied from sum_snaphsot_requested snr)


select ut.loc_code as loc_code, cl.loc_name as court_name,
sum(ut.no_requested)::integer as no_requested,
sum(ut.supplied)::integer as supplied
from union_tables ut
join juror_mod.court_location cl on ut.loc_code = cl.loc_code
group by ut.loc_code, cl.loc_name
order by cl.loc_name asc;

END;
$function$
;