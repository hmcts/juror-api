
-- Function to return jury summoning monitor stats by court
create or replace function juror_mod.jsm_report_by_court(p_loc_code_list varchar, p_start_date date, p_end_date date)
 returns table(
 	no_requested integer,
	deferrals_used integer,
	disq_on_selection integer,
	summoned integer,
	additional_summons integer,
	reminder integer,
	supplied integer,
	refused_excusal integer,
	refused_deferral integer,
	pnc_failed integer,
	disq_others integer,
	deferred_out integer,
	postponed integer,
	non_responded integer,
	undelivered integer,
	unavailable integer,
	moved_area integer,
	student integer,
	child_care integer,
	deceased integer,
	forces integer,
	financial_hardship integer,
	ill integer,
	toomany integer,
	criminal_record integer,
	"language" integer,
	medical integer,
	mental_health integer,
	other_excused integer,
	postponed_excused integer,
	religion integer,
	recently_served integer,
	travel integer,
	work_related integer,
	carer integer,
	holiday integer,
	bereavement integer,
	cjs_employee integer,
	deferred_court integer,
	personal_engagement integer,
	not_listed integer,
	awaiting_info integer,
	all_deferrals integer,
	all_postponements integer)

 language plpgsql
AS $function$
begin

return query

with current_data as (select coalesce((select sum(substring(ph.other_information,1,length(ph.other_information)-19)::integer)
                from juror_mod.pool_history ph
                where ph.pool_no = jp.pool_number
                and ph.history_code  = 'PHDI'
                and position('New' in ph.other_information) > 0),0) as deferrals_used,
sum(case when coalesce(jp.status,0)::text || coalesce(j.disq_code,'*') = '6A' then case when j.summons_file = 'disq. on selection' then 1 else 0 end else 0 end) as disq_on_selection,
coalesce((select sum(substring(ph.other_information,1,length(ph.other_information)-19)::integer)
                from juror_mod.pool_history ph
                where ph.pool_no = jp.pool_number
                and ph.history_code = 'PHSI'
                and position('New' in ph.other_information) > 0),0) as summoned,
coalesce((select sum(substring(ph.other_information,1,length(ph.other_information)-19)::integer)
                from juror_mod.pool_history ph
                where ph.pool_no = jp.pool_number
                and ph.history_code = 'PHSI'
                and position('Add' in ph.other_information) > 0),0) as additional_summons,
sum(case when coalesce(jp.reminder_sent,false) = true then 1 else 0 end) as reminder,
sum(case when coalesce(jp.status,0) = 2 then 1 else 0 end) as supplied,
sum(case when coalesce(jp.is_active,false) = true then case when j.acc_exc = 'Y' then 1 else 0 end else 0 end) as refused_excusal,
sum(case when coalesce(jp.is_active,false) = true then case when j.acc_exc = 'Z' then 1 else 0 end else 0 end) as refused_deferral,
sum(case when coalesce(jp.status,0)::text || coalesce(j.police_check,'*') = '6INELIGIBLE' then 1 else 0 end) as pnc_failed,
sum(case when coalesce(jp.status,0) = 6 then (case when coalesce(j.police_check,'*') <> 'INELIGIBLE' then 1 else 0 end) else 0 end) as disq_others,
sum(case when coalesce(jp.status,0)::text || coalesce(jp.is_active,false)::text = '7true' then case when jp.deferral_code = 'P' then 0 else 1 end else 0 end) as deferred_out,
sum(case when coalesce(jp.status,0)::text || coalesce(jp.deferral_code,'*') || coalesce(jp.is_active,false)::text = '7Ptrue' then 1 else 0 end) as postponed,
sum(case when coalesce(jp.status,0) = 1 then 1 else 0 end) as non_responded,
sum(case when coalesce(jp.status,0) = 9 then 1 else 0 end) as undelivered,
sum(case when coalesce(jp.is_active,false) = true then case when coalesce(jp.status,2) = 2 then 0 else 1 end else 0 end) as unavailable,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5A' then 1 else 0 end) as moved_area,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5B' then 1 else 0 end) as student,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5C' then 1 else 0 end) as child_care,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5D' then 1 else 0 end) as deceased,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5F' then 1 else 0 end) as forces,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5G' then 1 else 0 end) as financial_hardship,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5I' then 1 else 0 end) as ill,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5J' then 1 else 0 end) as toomany,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5K' then 1 else 0 end) as criminal_record,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5L' then 1 else 0 end) as "language",
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5M' then 1 else 0 end) as medical,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5N' then 1 else 0 end) as mental_health,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5O' then 1 else 0 end) as other_excused,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5P' then 1 else 0 end) as postponed_excused,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5R' then 1 else 0 end) as religion,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5S' then 1 else 0 end) as recently_served,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5T' then 1 else 0 end) as travel,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5W' then 1 else 0 end) as work_related,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5X' then 1 else 0 end) as carer,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5Y' then 1 else 0 end) as holiday,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5Z' then 1 else 0 end) as bereavement,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5CE' then 1 else 0 end) as cjs_employee,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5DC' then 1 else 0 end) as deferred_court,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5PE' then 1 else 0 end) as personal_engagement,
sum(case when coalesce(jp.status,0) = 5 and coalesce(j.excusal_code,'*') not in ('A','B','C','D','F','G','I','J','K','L','M','N','O','P','R','S','T','W','X','Y','Z','CE','DC','PE') then 1 else 0 end) as not_listed,
sum(case when coalesce(jr.processing_status,'*') in ('AWAITING_CONTACT','AWAITING_COURT_REPLY','AWAITING_TRANSLATION') then 1 else 0 end) as awaiting_info,
sum(case when coalesce(jp.status,0) = 7 then case when jp.deferral_code = 'P' then 0 else 1 end else 0 end) as all_deferrals,
sum(case when coalesce(jp.status,0)::text || coalesce(jp.deferral_code,'*') = '7P' then 1 else 0 end) as all_postponements
from juror_mod.juror_pool jp
join juror_mod.pool p on jp.pool_number = p.pool_no and p.loc_code in (select unnest(string_to_array(p_loc_code_list, ','))) and p.return_date between p_start_date and p_end_date
join juror_mod.juror j on jp.juror_number = j.juror_number
left join juror_mod.juror_response jr on jp.juror_number = jr.juror_number
where jp.owner = '400'
group by jp.pool_number),


snapshot_data as (select sum(case when coalesce(bs.reminder_sent,false) = true then 1 else 0 end) as reminder,
sum(case when coalesce(bs.status,0) = 2 then 1 else 0 end) as supplied,
sum(case when coalesce(bs.is_active,false) = true then case when bs.acc_exc = 'Y' then 1 else 0 end else 0 end) as refused_excusal,
sum(case when coalesce(bs.is_active,false) = true then case when bs.acc_exc = 'Z' then 1 else 0 end else 0 end) as refused_deferral,
sum(case when coalesce(bs.status,0) = 1 then 1 else 0 end) as non_responded,
sum(case when coalesce(jr.processing_status,'*') in ('AWAITING_CONTACT','AWAITING_COURT_REPLY','AWAITING_TRANSLATION') then 1 else 0 end) as awaiting_info
from juror_mod.bureau_snapshot bs
join juror_mod.pool p on bs.pool_number = p.pool_no and p.loc_code in (select unnest(string_to_array(p_loc_code_list, ','))) and p.return_date between p_start_date and p_end_date
and p."owner" <> '400'
left join juror_mod.juror_response jr on bs.juror_number = jr.juror_number
group by bs.pool_number),


sum_snapshot_data as (select coalesce(sum(s.reminder),0) as reminder,
coalesce (sum(s.supplied),0) as supplied,
coalesce (sum(s.refused_excusal), 0) as refused_excusal,
coalesce (sum(s.refused_deferral),0) as refused_deferral,
coalesce (sum(s.non_responded),0) as non_responded,
coalesce (sum(s.awaiting_info),0) as awaiting_info
from snapshot_data s
),

no_requested_with_bureau as (select p.no_requested as no_requested
from juror_mod.pool p
where p.loc_code in (select unnest(string_to_array(p_loc_code_list, ','))) and p.return_date between p_start_date and p_end_date
and p."owner" = '400'
group by p.pool_no),

no_requested_from_snapshot as (select p.no_requested as no_requested
from juror_mod.bureau_snapshot bs
join juror_mod.pool p on bs.pool_number = p.pool_no and p.loc_code in (select unnest(string_to_array(p_loc_code_list, ','))) and p.return_date between p_start_date and p_end_date
and p."owner" <> '400'
group by p.pool_no)


select (coalesce((select sum(nrb.no_requested) from no_requested_with_bureau nrb),0) + coalesce((select sum(nrs.no_requested) from no_requested_from_snapshot nrs),0))::integer as no_requested,
		sum(c.deferrals_used)::integer,
		sum(c.disq_on_selection)::integer,
		sum(c.summoned)::integer,
		sum(c.additional_summons)::integer,
		(sum(c.reminder) + (select s.reminder from sum_snapshot_data s))::integer as reminder,
		(sum(c.supplied) + (select s.supplied from sum_snapshot_data s))::integer as supplied,
		(sum(c.refused_excusal) + (select s.refused_excusal from sum_snapshot_data s))::integer as refused_excusal,
		(sum(c.refused_deferral) + (select s.refused_deferral from sum_snapshot_data s))::integer as refused_deferral,
		sum(c.pnc_failed)::integer,
		sum(c.disq_others)::integer,
		sum(c.deferred_out)::integer,
		sum(c.postponed)::integer,
		(sum(c.non_responded) + (select s.non_responded from sum_snapshot_data s) - sum(c.awaiting_info) - (select s.awaiting_info from sum_snapshot_data s))::integer as non_responded,
		sum(c.undelivered)::integer,
		(sum(c.unavailable) + (select s.non_responded from sum_snapshot_data s) + sum(c.awaiting_info) + (select s.awaiting_info from sum_snapshot_data s) - sum(c.undelivered))::integer as unavailable,
		sum(c.moved_area)::integer,
		sum(c.student)::integer,
		sum(c.child_care)::integer,
		sum(c.deceased)::integer,
		sum(c.forces)::integer,
		sum(c.financial_hardship)::integer,
		sum(c.ill)::integer,
		sum(c.toomany)::integer,
		sum(c.criminal_record)::integer,
		sum(c."language")::integer,
		sum(c.medical)::integer,
		sum(c.mental_health)::integer,
		sum(c.other_excused)::integer,
		sum(c.postponed_excused)::integer,
		sum(c.religion)::integer,
		sum(c.recently_served)::integer,
		sum(c.travel)::integer,
		sum(c.work_related)::integer,
		sum(c.carer)::integer,
		sum(c.holiday)::integer,
		sum(c.bereavement)::integer,
		sum(c.cjs_employee)::integer,
		sum(c.deferred_court)::integer,
		sum(c.personal_engagement)::integer,
		sum(c.not_listed)::integer,
		(sum(c.awaiting_info) + (select s.awaiting_info from sum_snapshot_data s))::integer as awaiting_info,
		sum(c.all_deferrals)::integer,
		sum(c.all_postponements)::integer
	from current_data c;

END;
$function$
;