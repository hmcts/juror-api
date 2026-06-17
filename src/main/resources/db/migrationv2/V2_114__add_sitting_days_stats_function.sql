

CREATE OR REPLACE PROCEDURE juror_dashboard.sitting_days_stats(IN no_of_months integer)
LANGUAGE plpgsql
AS $procedure$
/*
  Populates juror_mod.stats_sitting_days

  Replaces the latest n months of service months as specified by the no_of_months input parameter

  It is recommended to use no_of_months = 12 to allow for jurors on long trials

  Using the date of the jurors current pool to derive the service month
  A juror used in first week of the report period but with a start date before the first week of the report period
  (e.g. long trials) won't be reported because the start date of their pool is outside the report period

  Jurors that attended more than 1 local court across two months will be reported against the month of the juror's current pool/court for both courts

  e.g. Attended and used in last week of June for court 1 (primary)
       Reassigned to a pool with a start date in the first week of July for court 2 (satellite)
       Attended and used in July for court 2
       This report will use the date of the July pool to derive the service month for both courts
  */
declare

v_text_var1 text;
    v_text_var2 text;
    v_text_var3 text;
    temprow RECORD;

BEGIN

		-- delete rows for monthst that are to be refreshed
		-- not using Truncate Table as we only want to commit after the insert
delete from juror_mod.stats_sitting_days
where service_year >= to_char(date_trunc('MONTH',current_date - (no_of_months || ' month')::interval)::date,'YYYY')
  and service_month >= to_char(date_trunc('MONTH',current_date - (no_of_months || ' month')::interval)::date,'YYYY/MM');

FOR temprow IN

	        -- loop through each loc_code and collate utilisation stats and insert into the table.
SELECT cl.loc_code
from   juror_mod.court_location cl
WHERE  cl.loc_code <> '400'

  LOOP

insert into juror_mod.stats_sitting_days
select service_year, service_month, court_code,
       sitting_days_category, courts.loc_name,
       sum(number_of_sitting_days), count(1)
from juror_mod.court_location courts,
     (select to_char(p.return_date,'YYYY') service_year, to_char(p.return_date,'YYYY/MM') service_month, temprow.loc_code court_code,
             urm.juror_number, sum(urm.sitting) number_of_sitting_days,
             case when sum(urm.sitting) = 0 then '0'
                  when sum(urm.sitting) = 1 then '1'
                  when sum(urm.sitting) = 2 then '2 '
                  when sum(urm.sitting) = 3 then '3'
                  when sum(urm.sitting) = 4 then '4'
                  when sum(urm.sitting) = 5 then '5'
                  when sum(urm.sitting) = 6 then '6'
                  when sum(urm.sitting) = 7 then '7'
                  when sum(urm.sitting) = 8 then '8'
                  when sum(urm.sitting) = 9 then '9 '
                  when sum(urm.sitting) = 10 then '10'
                  else '11 or more' end sitting_days_category
      from juror_mod.util_report_main( temprow.loc_code,date_trunc('MONTH',current_date - (no_of_months || ' month')::interval)::date,current_date) urm, -- specify date range
           juror_mod.juror_pool jp, juror_mod.pool p
      where jp.juror_number = urm.juror_number
        and jp.is_active is true
        and p.pool_no  = jp.pool_number
        and p.return_date between date_trunc('MONTH',current_date - (no_of_months || ' month')::interval)::date and current_date -- pool start date is being used to derive the report month
      group by to_char(p.return_date,'YYYY'), to_char(p.return_date,'YYYY/MM'), court_code, urm.juror_number) stats
where courts.loc_code = stats.court_code
group by service_year, service_month, court_code, courts.loc_name, sitting_days_category;
END LOOP;

exception

    when others then
        get stacked diagnostics v_text_var1 = message_text,
            v_text_var2 = pg_exception_detail,
            v_text_var3 = pg_exception_hint;

        raise notice '%', 'sitting_days_stats failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

rollback;

end;

$procedure$
;
