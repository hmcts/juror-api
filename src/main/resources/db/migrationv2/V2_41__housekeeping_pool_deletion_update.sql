-- DROP PROCEDURE juror_mod.housekeeping_pool_deletion(in int4, in int4, in int4, inout text);

CREATE OR REPLACE PROCEDURE juror_mod.housekeeping_pool_deletion(IN p_max_threshold integer, IN p_start_time_int integer, IN p_max_timeout integer, INOUT p_print_msg text)
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
		WITH aged_pools
		AS
		(
			SELECT p.pool_no
			FROM juror_mod.pool p
			WHERE p.return_date < CURRENT_DATE - p_max_threshold
		)
		DELETE
		FROM juror_mod.pool_comments pc
		USING aged_pools ap
		WHERE pc.pool_no = ap.pool_no;

		-- check if timeout has elapsed - if so exit the process
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		WITH aged_pools
		AS
		(
			SELECT p.pool_no
			FROM juror_mod.pool p
			WHERE p.return_date < CURRENT_DATE - p_max_threshold
		)
		DELETE
		FROM juror_mod.pool_history ph
		USING aged_pools ap
		WHERE ph.pool_no = ap.pool_no;

		-- check if timeout has elapsed - if so exit the process
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		-- make sure we dont delete pools where there are juror_pool records still referring to them
		with old_pools as (
			select pool_no
			FROM juror_mod.pool p
			WHERE p.return_date < CURRENT_DATE - p_max_threshold
		), exclude_pools as (
 			select pool_no from old_pools op inner join juror_mod.juror_pool jp
 			on op.pool_no = jp.pool_number
		)
		delete from juror_mod.pool p using old_pools op
		where p.pool_no = op.pool_no and p.pool_no not in (select ep.pool_no from exclude_pools ep);


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

	        RAISE NOTICE '%', p_print_msg;
    END;
END;
$procedure$
;
