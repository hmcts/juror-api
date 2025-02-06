-- DROP PROCEDURE juror_dashboard.response_times(int4);

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
                                              and h2.history_code <> 'RSUM' -- ignore summons
                                              and h2.history_code <> 'RNRE' -- ignore reminder letters
                                              and h2.history_code <> 'PUND' -- Fix for JDB-4621: Undeliverable event is not a response to the summons
                                              and h2.history_code <> 'PREA' -- JDB-5349 : ignore the pool reasignment
                                              and h2.history_code <> 'RSUP' -- JDB-5374 : ignore summons reprinted
                                              and h2.user_id <> 'SYSTEM' -- filter out system generated excusals for covid19
                             left join 	juror_mod.juror_response r
                                          on 		r.juror_number = jp.juror_number
             where 		jp.pool_number in (select p.pool_no from juror_mod.pool p where p.return_date >= date_trunc('MONTH',current_date - (no_of_months || ' month')::interval))
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

-- DROP PROCEDURE juror_dashboard.not_responded(int4);

CREATE OR REPLACE PROCEDURE juror_dashboard.not_responded(IN no_of_months integer)
 LANGUAGE plpgsql
AS $procedure$
/*
 * Populates juror_dashboard.stats_not_responded
 * used by the Juror Digital performance dashboard
 *
 * Replaces the latest n months of summons counts as specified by the no_of_months input parameter
 *
 * It is recommended to use no_of_months = 6
 * 	- It needs to be at least 3 given jurors are summoned 9 weeks in advance of their attendance date
 * 	- Using 6 allows some contingency in case it is decided to summon jurors earlier
 *
 * Using delete then insert rather than merge due to the need to identify and delete rows that no longer have a non responded count
 *
 * Tables used:
 * 		juror_mod.juror j               Used to get the disqualified from selection indicator from the juror record i.e. summons_file
 * 		juror_mod.juror_pool jp         Used to get the loc_code from the juror record
 * 		juror_mod.juror_history h1      Used to get the summons date
 *      juror_mod.juror_history h2      Used to get the responded date if no entry in juror_response i.e. first event after the summons/reminders
 *                                      Chosen to not to rely on pool.status to indicate responded as incomplete responses may still show as Summoned.
 * 										13/3/24 Will be no need to use juror_history for this once no longer refreshing data migrated from Heritage
 * 		juror_mod.juror_response r      Online responses plus paper responses but the latter only from the go-live date for Juror Modernisation
 * 		juror_mod.pool p                Used to get a list of pool numbers to enable index on pool table to be used
 */
declare

    v_text_var1 text;
   	v_text_var2 text;
   	v_text_var3 text;
	l_Job_Type	varchar(50);

begin

	l_Job_Type := 'refresh_stats_data.not_responded';

	delete from juror_dashboard.stats_not_responded where summons_month >= date_trunc('month', current_date - (no_of_months || ' month')::interval);

	insert into juror_dashboard.stats_not_responded(
		select		date_trunc('month', s.summons_date) summons_month,
				   	s.loc_code,
				   	count(1) as "Non_Responded_Count"
		from (
			   	select 		substr(h1.pool_number,1,3) as "loc_code",  -- JDB-5346 see comments above
							jp.juror_number,
							case
									when j.juror_number is null then 'Paper'
									when r.reply_type = 'Digital' then 'Online'
									else 'Paper'
							end as "method",
							r.date_received as "response_date", -- digital plus paper responses but the latter is only those receieved post Juror Modernisation go_live
							min(h1.date_created) as "summons_date",
							min(h2.date_created) as "processed_date"
				from 		juror_mod.juror j
				join 		juror_mod.juror_pool jp
					on 		jp.juror_number = j.juror_number
				join 		juror_mod.juror_history h1
					on 		h1.juror_number = j.juror_number
							and h1.history_code = 'RSUM'
				left join	juror_mod.juror_history h2
					on 		h2.juror_number = jp.juror_number
							and h2.history_code <> 'RSUM' -- ignore summons
							and h2.history_code <> 'RNRE' -- ignore reminder letters
							and h2.history_code <> 'PUND' -- Fix for JDB-4621: Undeliverable event is not a response to the summons
							and h2.history_code <> 'PREA' -- JDB-5349 : ignore the pool reasignment
							and h2.history_code <> 'RSUP' -- JDB-5374 : ignore summons reprinted
				left join	juror_mod.juror_response r
					on 		r.juror_number = j.juror_number
				where 		jp.pool_number in (select p.pool_no from juror_mod.pool p where p.return_date >= date_trunc('MONTH',current_date - (no_of_months || ' month')::interval))
							and jp.is_active = true -- JDB-5346 see comments above
							and (j.summons_file is null or j.summons_file <> 'Disq. on selection')
							and h1.date_created > date_trunc('MONTH',current_date - (no_of_months || ' month')::interval)  -- exclude jurors summoned more than n months ago
							and coalesce(r.date_received, h2.date_created) is null -- exclude responded jurors
				group by 	substr(h1.pool_number,1,3),
							jp.juror_number,
							case
									when j.juror_number is null then 'Paper'
									when r.reply_type = 'Digital' then 'Online'
									else 'Paper'
							end,
							r.date_received
				order by 	jp.juror_number
			) s
		-- exclude responded jurors
		where 		coalesce(response_date, processed_date) is null
   		group by	summons_month, s.loc_code);

exception

	when others then
    	get stacked diagnostics	v_text_var1 = message_text,
                            	v_text_var2 = pg_exception_detail,
                            	v_text_var3 = pg_exception_hint;

	raise notice '%', 'not_responded failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

	rollback;

end;

$procedure$
;

-- DROP PROCEDURE juror_dashboard.thirdparty_online(int4);

CREATE OR REPLACE PROCEDURE juror_dashboard.thirdparty_online(IN no_of_months integer)
 LANGUAGE plpgsql
AS $procedure$
/*
 * Populates juror_dashboard.stats_thirdparty_online
 * used by the Juror Digital performance dashboard
 *
 * Number of third party respones by month summons issued
 *
 * Gets date of summons from part_hist
 *
 * Replaces the latest n months of summons months as specified by the no_of_months input parameter
 */
declare

    v_text_var1 text;
    v_text_var2 text;
    v_text_var3 text;
    l_job_type	varchar(50);

begin

    l_job_type := 'refresh_stats_data.thirdparty_online';

    insert into juror_dashboard.stats_thirdparty_online
    select 		date_trunc('month', h.date_created) summons_month, count(1) thirdparty_response_count
    from 		juror_mod.juror_response r
                    join 		juror_mod.juror_history h
                                on 		h.juror_number = r.juror_number
    where 		h.history_code = 'RSUM'
      and h.date_created > date_trunc('MONTH',current_date - (no_of_months || ' month')::interval)  -- exclude jurors summoned more than n months ago
      and r.relationship is not null
      and r.reply_type = 'Digital'
    group by 	date_trunc('month', h.date_created)
    ON CONFLICT(summons_month)
        DO UPDATE set thirdparty_response_count = EXCLUDED.thirdparty_response_count;

exception
    when others then
        get stacked diagnostics v_text_var1 = message_text,
            v_text_var2 = pg_exception_detail,
            v_text_var3 = pg_exception_hint;

        raise notice '%', 'thirdparty_online failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

        rollback;

end;

$procedure$
;

-- DROP PROCEDURE juror_dashboard.welsh_online_responses(int4);

CREATE OR REPLACE PROCEDURE juror_dashboard.welsh_online_responses(IN no_of_months integer)
 LANGUAGE plpgsql
AS $procedure$
/*
 * Populates juror_dashboard.stats_welsh_online_responses
 * used by the Juror Digital performance dashboard
 *
 * Number of summons by month summons issued, status and welsh flag
 *
 * Get date of summons from juror_history
 *
 * Replaces the latest n months of summons months as specified by the no_of_months input parameter
 */
declare

    v_text_var1 text;
    v_text_var2 text;
    v_text_var3 text;
    l_job_type	varchar(50);

begin

    l_job_type := 'refresh_stats_data.welsh_online_responses';

    insert into juror_dashboard.stats_welsh_online_responses
    select		date_trunc('month', h.date_created) as summons_month
         ,count(1) as welsh_response_count
    from 		juror_mod.juror_response r
                    join 		juror_mod.juror_history h
                                on 		h.juror_number = r.juror_number
    where 		h.history_code = 'RSUM'
      and h.date_created > (date_trunc('MONTH',current_date - (no_of_months || ' month')::interval)) -- exclude jurors summoned more than n months ago
      and r.welsh = true
      and r.reply_type = 'Digital'
    group by 	date_trunc('month', h.date_created)
    ON CONFLICT(summons_month)
        DO update set welsh_response_count = EXCLUDED.welsh_response_count;
exception
    when others then
        get stacked diagnostics	v_text_var1 = message_text,
            v_text_var2 = pg_exception_detail,
            v_text_var3 = pg_exception_hint;

        raise notice '%', 'welsh_online_responses failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

        rollback;

end;

$procedure$
;


