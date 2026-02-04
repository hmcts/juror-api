-- create table to hold collated service stats for SDP
CREATE TABLE juror_dashboard.stats_service (
	report_year varchar(4) NULL,
	report_month varchar(7) NULL,
	court_code varchar(3) NULL,
	court_name varchar(40) NULL,
	region varchar(30) NULL,
	jurors_supplied int4 NULL,
	working_days int4 NULL,
	sitting_days int4 NULL,
	non_sitting_days int4 NULL,
	attendance_days int4 NULL,
	non_attendance_days int4 NULL,
	number_of_servce_days int4 NULL,
	number_of_jurors int8 null,
	CONSTRAINT stats_service_pkey PRIMARY KEY (report_year, report_month, court_code),
	CONSTRAINT stats_service_court_code_fk FOREIGN KEY (court_code) REFERENCES juror_mod.court_location(loc_code)
	);


-- Drop redundant procedure
DROP PROCEDURE juror_dashboard.refresh_stats_data(int4, int4, int4, int4, int4);

-- Create function to refresh juror_dashboard.stats_service

CREATE OR REPLACE PROCEDURE juror_dashboard.service_stats(IN no_of_months integer)
 LANGUAGE plpgsql
AS $procedure$
/*
 * Populates juror_dashboard.stats_services
 *
 * Each run does a complete refresh of the latest number of whole months specified in no_of_months
 * e.g. passing in 7 * 12 as the number of months when run in the middle of January 2026
 *      will refresh the 7 years from 01/01/2029 upto and including the run date
 *
 * Note that the current month is always an incomplete month as not all attendances for the month will have been recorded
 */
declare

	v_text_var1 text;
   	v_text_var2 text;
   	v_text_var3 text;
	l_Job_Type	varchar(50);

begin

	l_Job_Type := 'refresh_stats_data.service';

	-- delete then insert
	-- not using truncate table as we want to commit the deeltes and inserts together
    delete from juror_dashboard.stats_service;

    insert into juror_dashboard.stats_service (
				select report_year, report_month, court_code, initcap(court_name), region_name, jurors_supplied_by_bureau,
				utilisation_working_days, utilisation_sitting_days, utilisation_non_sitting_days, utilisation_attendance_days,
				utilisation_non_attendance_days, service_length_number_of_days, service_length_number_of_jurors
				from juror_mod.service_stats_main(true,date_trunc('MONTH',current_date - (no_of_months || ' month')::interval)::date ));

exception

	when others then
		get stacked diagnostics	v_text_var1 = message_text,
                              	v_text_var2 = pg_exception_detail,
                              	v_text_var3 = pg_exception_hint;

    raise notice '%', 'service stats failed - error:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

    rollback;

end;

$procedure$
;
