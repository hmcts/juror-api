create or replace procedure juror_dashboard.refresh_stats_data(response_times_months int,
    welsh_online_months int,
    third_party_months int,
    excusals_months int, deferrals_months int) language plpgsql as

$$
-- Purpose : Procedures for populating staging tables used by the Juror Digital performance dashboard
-- Amended : 29/06/2020 Added THIRDPARTY_ONLINE procedure
-- Amended : 20/07/2020 Defect fix for RESPONSE_TIMES procedure - Group By neeed the Case statament not just the Decode
-- Amended : 27/10/2020 Defect fix for JDB-4621 Undeliverables counted as responded
-- Amended : 05/07/2021 Added DEFERRALS and EXCUSALS procedures
-- Amended : 05/08/2021 Defect fix for the Calendar Year calculation in the DEFERRALS and EXCUSALS procedures
-- Amended : 26/10/2021 JDB-4968 Defect fix for the Calendar Year calculation and exc_code in the DEFERRALS and EXCUSALS procedures
-- Amended : 14/12/2022 JDB-5310 Defect fix for missing DEFERRALS and EXCUSALS stats
-- Amended : 27/02/2023 JDB-5346 Defect fix for jurors whose original pool has been deleted
-- Amended : 02/03/2023 JDB-5349 The dashboard should ignore the pool reasignment history event 'PREA'
-- Amended : 02/05/2023 JDB-5374 The dashboard should ignore the summons reprinted 'RSUP' events (LH)
-- Amended : 13/03/2024 JM-6544 Refactor for Juror Modernisation changes to the data model

-- entry point
declare


begin

	call juror_dashboard.auto_processed();
	call juror_dashboard.response_times_and_not_responded(response_times_months);
	call juror_dashboard.unprocessed_responses();
	call juror_dashboard.welsh_online_responses(welsh_online_months);
	call juror_dashboard.thirdparty_online(third_party_months);
	call juror_dashboard.excusals(excusals_months);
	call juror_dashboard.deferrals(deferrals_months);

end;

$$;


create or replace procedure juror_dashboard.auto_processed()
language plpgsql as

$$
/**
 * Populates the stats_auto_processed table, runs 7 days a week and uses
 * max processed date to figure out the last date processed.
 *
 * No insertion of rows for days that have no AUTO processed responses
 *
 * Digital responses only.
 *
 * It will automatically catch-up if it hasn't been run for one or more days.
 *
 * To refresh existing rows, those rows would first need to be deleted from stats_auto_processed.
 */
declare

	v_text_var1 text;
   	v_text_var2 text;
   	v_text_var3 text;
	l_Job_Type	varchar(50);

begin

	l_Job_Type := 'refresh_stats_data.auto_processed';

	insert into juror_dashboard.stats_auto_processed (

		select		r.staff_assignment_date::date processed_date,
					count(1) as "count"
		from		juror_mod.juror_response r
		where		r.staff_login = 'AUTO'
					and r.staff_assignment_date::date > (
						select	coalesce(max(processed_date), '01-JAN-1990')
						from	juror_dashboard.stats_auto_processed)
					and r.staff_assignment_date::date < current_date
					and r.reply_type = 'Digital'
		group by	r.staff_assignment_date::date
	);

exception

	when others then
	        get stacked diagnostics v_text_var1 = message_text,
	                                v_text_var2 = pg_exception_detail,
	                                v_text_var3 = pg_exception_hint;

	raise notice '%', 'auto process failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

	rollback;

end;

$$;



create or replace procedure juror_dashboard.response_times_and_not_responded(no_of_months int)
language plpgsql as

$$
 /* Populates juror_dashboard.stats_not_responded and juror_dashboard.stats_response_times
  * used by the Juror Digital performance dashboard
  *
  * Populating both tables in a single transacton so that they are always consistant with each other
  *
  * Replaces the latest n months of summons counts as specified by the no_of_months input parameter
  *
  * It is recommended to use no_of_months = 6
  * 	- It needs to be at least 3 given jurors are summoned 9 weeks in advance of their attendance date
  * 	- Using 6 allows some contingency in case it is decided to summon jurors earlier
  */
declare

	v_text_var1 text;
   	v_text_var2 text;
   	v_text_var3 text;
	l_Job_Type	varchar(50);

begin

	l_Job_Type := 'refresh_stats_data.response_times_and_not_respond';

    call juror_dashboard.not_responded(no_of_months);
    call juror_dashboard.response_times(no_of_months);

exception

  	when others then
    	get stacked diagnostics v_text_var1 = message_text,
	                            v_text_var2 = pg_exception_detail,
	                            v_text_var3 = pg_exception_hint;

   raise notice '%', 'response times and non responses failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

	rollback;

end;

$$;


create or replace procedure juror_dashboard.unprocessed_responses()
language plpgsql as

$$
/*
 * Populates juror_dashboard.stats_unprocessed_responses
 * used by the Juror Digital performance dashboard
 */
declare

	v_text_var1 text;
   	v_text_var2 text;
   	v_text_var3 text;
	l_Job_Type	varchar(50);

begin

	l_Job_Type := 'refresh_stats_data.unprocessed_responses';

	-- delete then insert
	-- not using truncate table as we want to commit after the insert
    delete from juror_dashboard.stats_unprocessed_responses;

    insert into juror_dashboard.stats_unprocessed_responses (

    	select 		p.loc_code, count(1)
		from 		juror_mod.juror_pool jp
		join 		juror_mod.pool p
			on 		p.pool_no = jp.pool_number
		join 		juror_mod.juror_response r
			on 		r.juror_number = jp.juror_number
		where 		jp.is_active = true
					and r.juror_number = jp.juror_number
					and r.processing_status = 'TODO'
					and r.reply_type = 'Digital'
		group by	p.loc_code
	);

exception

	when others then
		get stacked diagnostics	v_text_var1 = message_text,
                              	v_text_var2 = pg_exception_detail,
                              	v_text_var3 = pg_exception_hint;

    raise notice '%', 'unprocessed responses failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

    rollback;

end;

$$;


create or replace procedure juror_dashboard.welsh_online_responses(no_of_months int)
language plpgsql as

$$
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
			        and h.date_created > (current_date - (no_of_months || ' month')::interval) -- exclude jurors summoned more than n months ago
			        and r.welsh = true
			        and r.reply_type = 'Digital'
        group by 	date_trunc('month', h.date_created)
	ON CONFLICT(summons_month)
    	DO update set welsh_response_count = welsh_response_count;
exception
	when others then
    	get stacked diagnostics	v_text_var1 = message_text,
                            	v_text_var2 = pg_exception_detail,
                            	v_text_var3 = pg_exception_hint;

   raise notice '%', 'welsh_online_responses failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

   rollback;

end;

$$;


create or replace procedure juror_dashboard.thirdparty_online(no_of_months int)
language plpgsql as

$$
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
        			and h.date_created > current_date - (no_of_months || ' month')::interval  -- exclude jurors summoned more than n months ago
        			and r.relationship is not null
        			and r.reply_type = 'Digital'
        group by 	date_trunc('month', h.date_created)
	ON CONFLICT(summons_month)
        DO UPDATE set thirdparty_response_count = thirdparty_response_count;

exception
	when others then
		get stacked diagnostics v_text_var1 = message_text,
        	                    v_text_var2 = pg_exception_detail,
            	                v_text_var3 = pg_exception_hint;

	raise notice '%', 'thirdparty_online failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

	rollback;

end;

$$;


create or replace procedure juror_dashboard.not_responded(no_of_months int)
language plpgsql as

$$
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
				where 		jp.pool_number in (select p.pool_no from juror_mod.pool p where p.return_date >= current_date - (no_of_months || ' month')::interval)
							and jp.is_active = true -- JDB-5346 see comments above
							and (j.summons_file is null or j.summons_file <> 'Disq. on selection')
							and h1.date_created > current_date - (no_of_months || ' month')::interval  -- exclude jurors summoned more than n months ago
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

$$;


create or replace procedure juror_dashboard.response_times(no_of_months int)
language plpgsql as

$$
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
 						when abs(coalesce(response_date, processed_date) - summons_date) < 8 then 'Within 7 days'
						when abs(coalesce(response_date, processed_date) - summons_date) < 15 then 'Within 14 days'
						when abs(coalesce(response_date, processed_date) - summons_date) < 22 then 'Within 21 days'
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
						r.date_received as response_date, -- digital plus paper responses but the latter is only those receieved post Juror Modernisation go_live
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
			where 		jp.pool_number in (select p.pool_no from juror_mod.pool p where p.return_date >= current_date - (no_of_months || ' month')::interval)
						and jp.is_active = true -- JDB-5346 see comments above
						and (j.summons_file is null or j.summons_file <> 'Disq. on selection')
						and h1.date_created > current_date - (no_of_months || ' month')::interval -- exclude jurors summoned more than n months ago
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
        DO update set response_count = response_count;
        
exception

	when others then
    	get stacked diagnostics v_text_var1 = message_text,
	                            v_text_var2 = pg_exception_detail,
    	                        v_text_var3 = pg_exception_hint;

	raise notice '%', 'response_times failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

	rollback;

end;

$$;


create or replace procedure juror_dashboard.deferrals(no_of_months int)
language plpgsql as

$$
/*
 *
 * Populates juror_dashboard.stats_deferrals used by the Juror Digital performance dashboard
 *
 * Replaces the latest n months of deferral stats as specified by the no_of_months input parameter
 *
 * It is recommended to use no_of_months = 12 to allow for deletion of deferrals
 *
 * Using delete then insert rather than merge due to the need to identify and deleted rows that no longer have an deferral count due to deletion of the deferral
 */
declare

	v_text_var1 text;
   	v_text_var2 text;
   	v_text_var3 text;
	l_Job_Type	varchar(50);

begin

	l_Job_Type := 'refresh_stats_data.deferrals';

	delete from juror_dashboard.stats_deferrals sd where sd.week >= to_char(date_trunc('week', current_date - no_of_months),'IYYY/IW');

    insert into juror_dashboard.stats_deferrals (
		select 		case
						when jp."owner" = '400' then 'Bureau'
		            	else 'Court'
		            end as "bureau_or_court",
		          	coalesce(jp.deferral_code,'O') as exec_code, -- default to O if null (allowing for inconsistent data in the pool table)
		          	to_char(p.return_date,'IYYY') as calendar_year,
		          	case
			          	when p.return_date < to_date('01-APR-'||to_char(p.return_date,'YYYY'),'DD-MON-YYYY') then (to_number(to_char(p.return_date,'YYYY'),'9999')-1)||'/'||to_char(p.return_date,'YY')
		              	else to_char(p.return_date,'YYYY')||'/'||(to_number(to_char(p.return_date,'YY'),'9999')+1)
		          	end as fin_year,
					to_char(p.return_date,'IYYY/IW') as "week",
		 			count(1)
		from		juror_mod.juror_pool jp
		inner join 	juror_mod.pool p
			on		jp.pool_number = p.pool_no
		where 		jp.status = 7
          			and p.return_date >= date_trunc('WEEK',current_date - (no_of_months || ' month')::interval) -- from the start of the week after deducting the no_of_months
		group by 	case
						when jp."owner" = '400' then 'Bureau'
            			else 'Court'
            		end,
					coalesce(jp.deferral_code,'O'),
					to_char(p.return_date,'IYYY'),
					case
						when p.return_date < to_date('01-APR-'||to_char(p.return_date,'YYYY'),'DD-MON-YYYY') then (to_number(to_char(p.return_date,'YYYY'),'9999')-1)||'/'||to_char(p.return_date,'YY')
              			else to_char(p.return_date,'YYYY')||'/'||(to_number(to_char(p.return_date,'YY'),'9999')+1)
          			end,
          			to_char(p.return_date,'IYYY/IW'));

exception

	when others then
    	get stacked diagnostics v_text_var1 = message_text,
                            	v_text_var2 = pg_exception_detail,
                            	v_text_var3 = pg_exception_hint;

    raise notice '%', 'deferrals failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

	rollback;

end;

$$;


create or replace procedure juror_dashboard.excusals(no_of_months int)
language plpgsql as

$$

declare

	v_text_var1 text;
   	v_text_var2 text;
   	v_text_var3 text;
	l_Job_Type	varchar(50);

begin

    l_Job_Type := 'refresh_stats_data.excusals';

   delete from juror_dashboard.stats_excusals where week >= to_char(date_trunc('week',current_date - (no_of_months || ' month')::interval),'IYYY/IW');

   insert into juror_dashboard.stats_excusals (
   		select 		case
						when jp."owner" = '400' then 'Bureau'
            			else 'Court'
            		end "bureau_or_court",
			        coalesce(j.excusal_code, 'O'), -- default to O if null (allowing for inconsistent data in the pool table)
			        to_char(p.return_date,'IYYY') calendar_year,
			        case
				        when p.return_date < to_date('01-APR-'||to_char(p.return_date,'YYYY'),'DD-MON-YYYY') then (to_number(to_char(p.return_date,'YYYY'),'9999')-1)||'/'||to_char(p.return_date,'YY')
						else to_char(p.return_date,'YYYY')||'/'||(to_number(to_char(p.return_date,'YY'),'9999')+1)
					end fin_year,
			        to_char(p.return_date,'IYYY/IW') as "week",
			        count(1)
        from 		juror_mod.juror_pool jp
		inner join	juror_mod.pool p
			on		jp.pool_number = p.pool_no
		inner join	juror_mod.juror j
			on		j.juror_number = jp.juror_number
        where 		jp.status = 5
			        and p.return_date >= date_trunc('week',current_date - (no_of_months || ' month')::interval) -- from the start of the week after deducting the no_of_months
			        and jp.is_active = true
        group by 	case
	        			when jp."owner" = '400' then 'Bureau'
            			else 'Court'
            		end,
			        coalesce(j.excusal_code,'O'),
			        to_char(p.return_date,'IYYY'),
			        case when p.return_date < to_date('01-APR-'||to_char(p.return_date,'YYYY'),'DD-MON-YYYY') then (to_number(to_char(p.return_date,'YYYY'),'9999')-1)||'/'||to_char(p.return_date,'YY')
			             else to_char(p.return_date,'YYYY')||'/'||(to_number(to_char(p.return_date,'YY'),'9999')+1) end,
			        to_char(p.return_date,'IYYY/IW'));

exception

	when others then
    	get stacked diagnostics v_text_var1 = message_text,
        	                    v_text_var2 = pg_exception_detail,
            	                v_text_var3 = pg_exception_hint;

   raise notice '%', 'unprocessed responses failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

  rollback;

end;

$$;