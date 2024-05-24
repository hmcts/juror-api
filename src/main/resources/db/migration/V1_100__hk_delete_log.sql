
CREATE OR REPLACE PROCEDURE juror_mod.hk_delete_log(p_audit_threshold INTEGER DEFAULT 3652)
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
  	v_print_msg TEXT;
    v_text_var1 TEXT;
   	v_text_var2 TEXT;
   	v_text_var3 TEXT;

BEGIN
	DELETE FROM juror_mod.hk_run_log hrl WHERE hrl.end_time < CURRENT_DATE - p_audit_threshold;	-- default to 10 years if not set in params

    EXCEPTION

    	WHEN OTHERS THEN

        GET STACKED DIAGNOSTICS v_text_var1 = MESSAGE_TEXT,
                                v_text_var2 = PG_EXCEPTION_DETAIL,
                                v_text_var3 = PG_EXCEPTION_HINT;

        v_print_msg := 'ERROR:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

        RAISE NOTICE '%', v_print_msg;	
END;
$$

