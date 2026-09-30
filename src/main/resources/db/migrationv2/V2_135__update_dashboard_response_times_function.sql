CREATE OR REPLACE PROCEDURE juror_dashboard.response_times(IN no_of_months integer)
 LANGUAGE plpgsql
AS $procedure$
/*
 * Populates juror_dashboard.stats_response_times used by the Juror Digital performance dashboard
 *
 * Replaces the latest n months of summons months as specified by the no_of_months input parameter
 *
 * It is recommended to use no_of_months = 6
 * 	- It needs to be at least 3 given jurors are summoned 9 weeks in advance of their attendance date
 * 	- Using 6 allows some contingency in case it is decided to summon jurors earlier
 *
 * This uses a list of pools with start dates that fall within n months of the current month inorder to aid performance
 * Given pools are summoned 2 months in advance, any jurors moved to pools earlier than the date that they were summoned
 * will still be counted, provided the start date of the new pool is no more than 2 months earlier than the summons date
 * In practise, only 13 jurors have had their servcie brought forward to an earlier month and all 13 were within 2 months of the original month
 *
 * Deletion of a pool after jurors have been moved to another pool has no adverse impact
 * This function doesn't join on the original pool specified in the RSUM event, it just uses it to get the loc_code of the sumnmons
 *
 */
declare

v_text_var1 text;
    v_text_var2 text;
    v_text_var3 text;
    l_job_type	varchar(50);

begin

    l_job_type := 'refresh_stats_data.stats_response_times';

insert into juror_dashboard.stats_response_times
select 		date_trunc('month', s.summons_date) as summons_month,
          date_trunc('month', coalesce(response_date, processed_date)) as response_month,
          case
            when abs(coalesce(response_date::date, processed_date::date) - summons_date::date) < 8 then 'Within 7 days'
            when abs(coalesce(response_date::date, processed_date::date) - summons_date::date) < 15 then 'Within 14 days'
            when abs(coalesce(response_date::date, processed_date::date) - summons_date::date) < 22 then 'Within 21 days'
            else 'Over 21 days'
            end response_period,
          s.loc_code,
          s.method Response_Method,
          count(1) Response_Count
from (
       select		substr(h1.pool_number,1,3) as loc_code,  -- JDB-5346 see comments above
                 jp.juror_number,
                 case
                   when r.juror_number is null then 'Paper'
                   when r.reply_type = 'Digital' then 'Online'
                   else 'Paper'
                   end as "method",
                 r.date_received as response_date, -- digital plus paper responses but the latter is only those received post Juror Modernisation go_live
                 min(h1.date_created) as summons_date,
                 min(h2.date_created) as processed_date
       from 		juror_mod.juror j
                 inner join 	juror_mod.juror_pool jp
                             on 		jp.juror_number = j.juror_number
                 inner join 	juror_mod.juror_history h1
                             on 		h1.juror_number = j.juror_number
                               and h1.history_code = 'RSUM'
                 left join 	juror_mod.juror_history h2
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
                            on 		r.juror_number = jp.juror_number
       where jp.pool_number in (select p.pool_no from juror_mod.pool p where p.return_date >= date_trunc('MONTH',current_date - (no_of_months || ' month')::interval))
         and jp.is_active = true -- JDB-5346 see comments above
         and (j.summons_file is null or j.summons_file <> 'Disq. on selection')
         and h1.date_created > date_trunc('MONTH',current_date - (no_of_months || ' month')::interval) -- exclude jurors summoned more than n months ago
       group by 	substr(h1.pool_number,1,3), jp.juror_number,
                 case
                   when r.juror_number is null then 'Paper'
                   when r.reply_type = 'Digital' then 'Online'
                   else 'Paper'
                   end,
                 r.date_received
       order by 	jp.juror_number
     ) s
where 		coalesce(response_date, processed_date) is not null -- exclude non responded
group by 	date_trunc('month', s.summons_date),
          date_trunc('month', coalesce(response_date, processed_date)),
          case
            when abs(coalesce(response_date::date, processed_date::date) - summons_date::date) < 8 then 'Within 7 days'
            when abs(coalesce(response_date::date, processed_date::date) - summons_date::date) < 15 then 'Within 14 days'
            when abs(coalesce(response_date::date, processed_date::date) - summons_date::date) < 22 then 'Within 21 days'
            else 'Over 21 days'
            end,
          s.loc_code, s."method"
  ON CONFLICT(summons_month, response_month, response_period, loc_code, response_method)
        DO update set response_count = EXCLUDED.response_count;

exception

    when others then
        get stacked diagnostics v_text_var1 = message_text,
            v_text_var2 = pg_exception_detail,
            v_text_var3 = pg_exception_hint;

        raise notice '%', 'response_times failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

rollback;

end;

$procedure$
;
