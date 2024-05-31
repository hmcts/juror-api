CREATE OR REPLACE PROCEDURE juror_mod.housekeeping_log_deletion(p_max_threshold INTEGER, p_start_time_int INTEGER, p_max_timeout INTEGER, INOUT p_print_msg text)
LANGUAGE plpgsql
as
/***********************************************************************************************************************
*  Author  : Andrew Fraser
*  Created : 12 Feb 2024
*  Purpose : Housekeeping - remove aged event log data.
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
	DELETE 
	FROM juror_mod.hk_run_log hrl 
	WHERE hrl.end_time < CURRENT_DATE - p_max_threshold;	-- default to 10 years if not set in params

	-- check if timeout has elapsed - if so exit the process
    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
	IF v_timed_out THEN
		p_print_msg := 'ERROR:-> TIMED OUT';
	END IF;

    EXCEPTION

    	WHEN OTHERS THEN

        GET STACKED DIAGNOSTICS v_text_var1 = MESSAGE_TEXT,
                                v_text_var2 = PG_EXCEPTION_DETAIL,
                                v_text_var3 = PG_EXCEPTION_HINT;

        p_print_msg := 'DELETE FAILED - ERROR:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

        RAISE NOTICE '%', p_print_msg;	
END;
$$
