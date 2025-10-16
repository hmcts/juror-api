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

	delete from juror_dashboard.stats_deferrals sd where sd.week >= to_char(date_trunc('WEEK',current_date - (no_of_months|| ' month')::interval),'IYYY/IW');

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
