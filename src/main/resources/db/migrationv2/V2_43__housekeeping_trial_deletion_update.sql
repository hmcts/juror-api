-- DROP PROCEDURE juror_mod.housekeeping_trial_deletion(in int4, in int4, in int4, inout text);

CREATE OR REPLACE PROCEDURE juror_mod.housekeeping_trial_deletion(IN p_max_threshold integer, IN p_start_time_int integer, IN p_max_timeout integer, INOUT p_print_msg text)
 LANGUAGE plpgsql
AS $procedure$
DECLARE
   	v_text_var1 TEXT;
   	v_text_var2 TEXT;
   	v_text_var3 TEXT;
   	v_timed_out BOOLEAN;

BEGIN
	<<Deletes>>
	BEGIN
		-- child records
		WITH aged_trials AS
		(
			SELECT t.trial_number, t.loc_code
			FROM juror_mod.trial t
			WHERE t.trial_end_date < CURRENT_DATE - p_max_threshold
		), excluded_trials AS
    (
      SELECT jt.trial_number
      FROM juror_mod.juror_trial jt
      INNER JOIN aged_trials agt ON jt.trial_number = agt.trial_number and jt.loc_code = agt.loc_code
    )
		DELETE
		FROM juror_mod.accused a
		USING aged_trials agt
		WHERE a.trial_number = agt.trial_number and a.loc_code = agt.loc_code and agt.trial_number not in (select et.trial_number from excluded_trials et);

		-- check if timeout has elapsed - if so exit the process
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		-- delete the parent record where there is no reference to it from juror_trial
		WITH aged_trials AS
		(
			SELECT t.trial_number, t.loc_code
			FROM juror_mod.trial t
			WHERE t.trial_end_date < CURRENT_DATE - p_max_threshold
		), excluded_trials AS
    (
      SELECT jt.trial_number
      FROM juror_mod.juror_trial jt
      INNER JOIN aged_trials agt ON jt.trial_number = agt.trial_number and jt.loc_code = agt.loc_code
    )
		DELETE
		FROM juror_mod.trial t
		USING aged_trials agt
		WHERE t.trial_number = agt.trial_number and t.loc_code = agt.loc_code and agt.trial_number not in (select et.trial_number from excluded_trials et);

		-- check if timeout has elapsed - if so exit the process
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		-- upon error...
	    EXCEPTION

	    	WHEN OTHERS THEN

	        GET STACKED DIAGNOSTICS v_text_var1 = MESSAGE_TEXT,
	                                v_text_var2 = PG_EXCEPTION_DETAIL,
	                                v_text_var3 = PG_EXCEPTION_HINT;

	        p_print_msg := 'DELETE FAILED - ERROR:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

	END;
END;
$procedure$
;
