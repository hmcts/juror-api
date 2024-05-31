CREATE OR REPLACE PROCEDURE juror_mod.housekeeping_juror_digital_deletion(p_juror_number VARCHAR(9), p_threshold INTEGER, p_start_time_int INTEGER, p_max_timeout INTEGER, INOUT p_print_msg text)
LANGUAGE plpgsql
AS
/***********************************************************************************************************************
*  Author  : Andrew Fraser
*  Created : 16 Feb 2024
*  Purpose : Housekeeping for Juror parent and child records, including pool related data.
* 
*   Change History:
*
*   Ver  Date     Author     Description
*   ---  ----     ------     -----------
*
***********************************************************************************************************************/
$$
DECLARE
   	v_text_var1 TEXT;
   	v_text_var2 TEXT;
   	v_text_var3 TEXT;
   	v_timed_out BOOLEAN;

BEGIN
	-- Perform the deletion 
	BEGIN

	    DELETE FROM juror_mod.juror_reasonable_adjustment jra WHERE jra.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'DELETE FAILED - ERROR:-> TIMED OUT';
		END IF;

		DELETE FROM juror_mod.juror_response_aud jra WHERE jra.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'DELETE FAILED - ERROR:-> TIMED OUT';
		END IF;

		DELETE FROM juror_mod.juror_response_cjs_employment jrce WHERE jrce.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'DELETE FAILED - ERROR:-> TIMED OUT';
		END IF;

		DELETE FROM juror_mod.user_juror_response_audit ujra WHERE ujra.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'DELETE FAILED - ERROR:-> TIMED OUT';
		END IF;

		DELETE FROM juror_mod.juror_response jr WHERE jr.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'DELETE FAILED - ERROR:-> TIMED OUT';
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
$$
