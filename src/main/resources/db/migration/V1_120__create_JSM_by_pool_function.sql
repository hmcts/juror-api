
-- Function to return jury summoning monitor stats for a pool
create or replace function  juror_mod.jsm_report_by_pool(p_pool_number text)
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
	by_right integer,
	child_care  integer,
	work_related integer,
	medical integer,
	travel integer,
	student integer,
	moved_out integer,
	"language" integer,
	other_excused integer,
	deferred_out integer,
	postponed integer,
	non_responded integer,
	undelivered integer,
	unavailable integer,
	mental integer,
	toomany integer,
	religion integer,
	carer integer,
	deceased integer,
	holiday integer,
	postponed_excused integer,
	old_or_new integer,
	awaiting_info integer,
	all_deferrals integer,
	all_postponements integer)
 language plpgsql
AS $function$
begin

return query

with current_data as (select p.no_requested,
coalesce((select sum(substring(ph.other_information,1,length(ph.other_information)-19)::integer)
                from juror_mod.pool_history ph
                where ph.pool_no = jp.pool_number
                and ph.history_code  = 'PHDI'
                and position('New' in ph.other_information) > 0
                and ph.pool_no = p_pool_number),0) as deferrals_used,
sum(case when coalesce(jp.status,0)::text || coalesce(j.disq_code,'*') = '6A' then case when summons_file = 'disq. on selection' then 1 else 0 end else 0 end) as disq_on_selection,
coalesce((select sum(substring(ph.other_information,1,length(ph.other_information)-19)::integer)
                from juror_mod.pool_history ph
                where ph.pool_no = jp.pool_number
                and ph.history_code = 'PHSI'
                and position('New' in ph.other_information) > 0
                and ph.pool_no = p_pool_number),0) as summoned,
coalesce((select sum(substring(ph.other_information,1,length(ph.other_information)-19)::integer)
                from juror_mod.pool_history ph
                where ph.pool_no = jp.pool_number
                and ph.history_code = 'PHSI'
                and position('Add' in ph.other_information) > 0
                and ph.pool_no  = p_pool_number),0) as additional_summons,
sum(case when coalesce(reminder_sent,false) = true then 1 else 0 end) as reminder,
sum(case when coalesce(status,0) = 2 then 1 else 0 end) as supplied,
sum(case when coalesce(jp.is_active,false) = true then case when j.acc_exc = 'Y' then 1 else 0 end else 0 end) as refused_excusal,
sum(case when coalesce(jp.is_active,false) = true then case when j.acc_exc = 'Z' then 1 else 0 end else 0 end) as refused_deferral,
sum(case when coalesce(j.police_check,'*') = 'INELIGIBLE' then 1 else 0 end) as pnc_failed,
sum(case when coalesce(jp.status,0) = 6 then (case when coalesce(j.police_check,'*') <> 'INELIGIBLE' then 1 else 0 end) else 0 end) as disq_others,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5S' then 1 else 0 end) as by_right,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5C' then 1 else 0 end) as child_care,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') in ('5W', '5F') then 1 else 0 end) as work_related,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5M' then 1 else 0 end) as medical,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5T' then 1 else 0 end) as travel,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5B' then 1 else 0 end) as student,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5A' then 1 else 0 end) as moved_out,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5L' then 1 else 0 end) as "language",
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5O' then 1 else 0 end) as other_excused,
sum(case when coalesce(jp.status,0)::text || coalesce(jp.is_active,false)::text = '7true' then case when j.excusal_code = 'P' then 0 else 1 end else 0 end) as deferred_out,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') || coalesce(jp.is_active,false)::text = '7Ptrue' then 1 else 0 end) as postponed,
sum(case when coalesce(jp.status,0) = 1 then 1 else 0 end) as non_responded,
sum(case when coalesce(jp.status,0) = 9 then 1 else 0 end) as undelivered,
sum(case when coalesce(jp.is_active,false) = true then case when coalesce(status,2) = 2 then 0 else 1 end else 0 end) as unavailable,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5N' then 1 else 0 end) as mental,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5J' then 1 else 0 end) as toomany,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5R' then 1 else 0 end) as religion,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5X' then 1 else 0 end) as carer,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5D' then 1 else 0 end) as deceased,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5Y' then 1 else 0 end) as holiday,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '5P' then 1 else 0 end) as postponed_excused,
sum(case when coalesce(jp.status,0) = 5 then case when coalesce(j.excusal_code,'*') not in ('S','C','W','F','M','T','B','A','L','O','N','J','R','X','D','Y','P') then 1 else 0 end else 0 end) as old_or_new,
sum(case when coalesce(jr.processing_status,'*') in ('AWAITING_CONTACT','AWAITING_COURT_REPLY','AWAITING_TRANSLATION') then 1 else 0 end) as awaiting_info,
sum(case when coalesce(jp.status,0) = 7 then case when j.excusal_code = 'P' then 0 else 1 end else 0 end) as all_deferrals,
sum(case when coalesce(jp.status,0)::text || coalesce(j.excusal_code,'*') = '7P' then 1 else 0 end) as all_postponements
from juror_mod.juror_pool jp
join juror_mod.pool p on jp.pool_number = p.pool_no and jp.owner = p.owner
join juror_mod.juror j on jp.juror_number = j.juror_number
left join juror_mod.juror_response jr on jp.juror_number = jr.juror_number
where jp.owner = '400'
and jp.pool_number = p_pool_number
group by jp.pool_number, p.no_requested),

snapshot_data as (select p.no_requested,
sum(case when coalesce(bs.reminder_sent,false) = true then 1 else 0 end) as reminder,
sum(case when coalesce(bs.status,0) = 2 then 1 else 0 end) as supplied,
sum(case when coalesce(bs.is_active,false) = true then case when bs.acc_exc = 'Y' then 1 else 0 end else 0 end) as refused_excusal,
sum(case when coalesce(bs.is_active,false) = true then case when bs.acc_exc = 'Z' then 1 else 0 end else 0 end) as refused_deferral,
sum(case when coalesce(bs.status,0) = 1 then 1 else 0 end) as non_responded
from juror_mod.bureau_snapshot bs
join juror_mod.pool p on bs.pool_number = p.pool_no
where bs.pool_number = p_pool_number
group by bs.pool_number, p.no_requested)

select coalesce ((select s.no_requested from snapshot_data s),coalesce(c.no_requested,0),0) as no_requested,
		c.deferrals_used::integer,
		c.disq_on_selection::integer,
		c.summoned::integer,
		c.additional_summons::integer,
		(c.reminder + coalesce((select s.reminder from snapshot_data s), 0))::integer as reminder,
		(c.supplied + coalesce((select s.supplied from snapshot_data s), 0))::integer as supplied,
		(c.refused_excusal + coalesce((select s.refused_excusal from snapshot_data s),0))::integer as refused_excusal,
		(c.refused_deferral + coalesce((select s.refused_deferral from snapshot_data s),0))::integer as refused_deferral,
		c.pnc_failed::integer,
		c.disq_others::integer,
		c.by_right::integer,
		c.child_care::integer,
		c.work_related::integer,
		c.medical::integer,
		c.travel::integer,
		c.student::integer,
		c.moved_out::integer,
		c.language::integer,
		c.other_excused::integer,
		c.deferred_out::integer,
		c.postponed::integer,
		(c.non_responded + coalesce((select s.refused_deferral from snapshot_data s),0))::integer as non_responded,
		c.undelivered::integer,
		c.unavailable::integer,
		c.mental::integer,
		c.toomany::integer,
		c.religion::integer,
		c.carer::integer,
		c.deceased::integer,
		c.holiday::integer,
		c.postponed_excused::integer,
		c.old_or_new::integer,
		c.awaiting_info::integer,
		c.all_deferrals::integer,
		c.all_postponements::integer
	from current_data c;

END;
$function$
;