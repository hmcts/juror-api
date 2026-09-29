CREATE OR REPLACE PROCEDURE juror_dashboard.dbd_responses(IN no_of_months integer)
LANGUAGE plpgsql
AS $procedure$
/*
* Populates juror_dashboard.dbd_response_stats used by the Juror DBD performance dashboard
*
* Replaces the latest n months of summons months as specified by the no_of_months input parameter
*
* It is recommended to use no_of_months = 6
* 	- It needs to be at least 3 given jurors are summoned 9 weeks in advance of their attendance date
* 	- Using 6 allows some contingency in case it is decided to summon jurors earlier
*
* Having to use Delete/Insert rather than On Conflict Update as we need to ensure that rows that no longer have
* a juror count are deleted e.g. for when all jurors in a pool have responded, the Not responded rows need to be deleted
*
*/
declare

    v_text_var1 text;
    v_text_var2 text;
    v_text_var3 text;
    l_job_type	varchar(50);

begin

    l_job_type := 'refresh_stats_data.dbd_response_stats';

	delete from juror_dashboard.dbd_response_stats
	where summons_date >= date_trunc('MONTH',current_date - (no_of_months || ' month')::interval);

    insert into juror_dashboard.dbd_response_stats(summons_date, response_date, response_period, loc_code, response_method, age_group, juror_count)

	  select	s.summons_date::date as summons_date,
				coalesce(response_date, processed_date) as response_date,
				case when coalesce(response_date, processed_date) is null then 'Not Responded'
					 when abs(coalesce(response_date, processed_date) - summons_date) < 8 then 'Within 7 days'
					 when abs(coalesce(response_date, processed_date) - summons_date) < 15 then 'Within 14 days'
					 when abs(coalesce(response_date, processed_date) - summons_date) < 22 then 'Within 21 days'
					 else 'Over 21 days' end response_period,
				s.loc_code,
				case when coalesce(response_date, processed_date) is null then 'None' else s.method end Response_Method,
				case when age_in_years is null then 'Unknown'
					 when age_in_years < 18 then 'Less than 18'
					 when age_in_years < 31 then '18 to 30'
					 when age_in_years < 41 then '31 to 40'
					 when age_in_years < 51 then '41 to 50'
					 when age_in_years < 61 then '51 to 60'
					 when age_in_years < 71 then '61 to 70'
					 when age_in_years < 76 then '71 to 75'
					 when age_in_years > 75 then 'Over 75'
					 else 'Unknown' end age_group,
				count(1) Response_Count
			from (select substr(h1.pool_number,1,3) as loc_code,  -- JDB-5346 see comments above
						jp.juror_number,
						case when r.juror_number is null then 'Paper'
							 when r.reply_type = 'Digital' then 'Online'
							 else 'Paper' end as "method",
						r.date_received::date as response_date, -- digital plus paper responses but the latter is only those received post Juror Modernisation go_live
						j.dob, extract('Year' from age(min(h1.date_created),dob)) age_in_years,
						min(h1.date_created::date) as summons_date,
						min(h2.date_created::date) as processed_date
				from 	juror_mod.juror j
				inner join	juror_mod.juror_pool jp
							 on jp.juror_number = j.juror_number
				inner join	juror_mod.juror_history h1
							 on h1.juror_number = j.juror_number
							and h1.history_code = 'RSUM'
				left join	juror_mod.juror_history h2
							 on h2.juror_number = j.juror_number
	                        and h2.history_code <> 'RSUM' -- Ignore Summons
	                        and h2.history_code <> 'RNRE' -- Ignore Non Responded letters (Reminder)
	                        and h2.history_code <> 'RSUP' -- JDB-5374 : ignore Summons Reissue
	                        and h2.history_code <> 'PUND' -- JDB-4621 : ignore Undeliverable
	                        and h2.history_code <> 'PREA' -- JDB-5349 : ignore pool reassignment
	                        and h2.history_code <> 'RCPK' -- JS-1152 : ignore Issue Response Pack
	                        and h2.history_code <> 'RLPK' -- JS-1152 : ignore Reissue Response Pack
	                        and h2.history_code <> 'RLNR' -- JS-1152 : ignore Reissue Non Responsed Letter (Reminder)
	                        and h2.history_code <> 'RMES' -- JS-1152 : ignore Contact Details Export
	                        and h2.user_id <> 'SYSTEM' -- filter out system generated adhoc events e.g. excusals during covid19 in 2020
				left join 	juror_mod.juror_response r
							 on r.juror_number = jp.juror_number
				where jp.pool_number in (select p.pool_no from juror_mod.pool p
										 where p.return_date >= date_trunc('MONTH',current_date - (no_of_months || ' month')::interval))
									     -- not filtering on just DBD courts to allow for DBD jurors moved between courts
				  and jp.is_active = true
				  and (j.summons_file is null or j.summons_file <> 'Disq. on selection')
				  and h1.date_created > date_trunc('MONTH',current_date - (no_of_months || ' month')::interval) -- exclude jurors summoned more than n months ago
                  and substr(h1.pool_number,1,3) in (select loc_code from juror_mod.court_location c where c.digital_by_default is true) -- DBD pilot courts
				group by	substr(h1.pool_number,1,3), jp.juror_number,
							case when r.juror_number is null then 'Paper'
								 when r.reply_type = 'Digital' then 'Online'
								 else 'Paper' end,
							j.dob,
							r.date_received::date ) s
		 group by	s.summons_date,
					coalesce(response_date, processed_date),
					case when coalesce(response_date, processed_date) is null then 'Not Responded'
						 when abs(coalesce(response_date, processed_date::date) - summons_date) < 8 then 'Within 7 days'
						 when abs(coalesce(response_date, processed_date::date) - summons_date) < 15 then 'Within 14 days'
						 when abs(coalesce(response_date, processed_date::date) - summons_date) < 22 then 'Within 21 days'
						 else 'Over 21 days' end,
					s.loc_code, case when coalesce(response_date::date, processed_date::date) is null then 'None' else s.method end,
					case when age_in_years is null then 'Unknown'
						 when age_in_years < 18 then 'Less than 18'
						 when age_in_years < 31 then '18 to 30'
						 when age_in_years < 41 then '31 to 40'
						 when age_in_years < 51 then '41 to 50'
						 when age_in_years < 61 then '51 to 60'
						 when age_in_years < 71 then '61 to 70'
						 when age_in_years < 76 then '71 to 75'
						 when age_in_years > 75 then 'Over 75'
						 else 'Unknown' end;

exception

    when others then
        get stacked diagnostics v_text_var1 = message_text,
            v_text_var2 = pg_exception_detail,
            v_text_var3 = pg_exception_hint;

        raise notice '%', 'DBD_response_stats failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

        rollback;

end;

$procedure$
;
