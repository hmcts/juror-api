CREATE OR REPLACE PROCEDURE juror_mod.housekeeping_utilisation_stats_deletion(p_max_threshold INTEGER, p_start_time_int INTEGER, p_max_timeout INTEGER, INOUT p_print_msg text)
LANGUAGE plpgsql
as
/***********************************************************************************************************************
*  Author  : Andrew Fraser
*  Created : 28 Feb 2024
*  Purpose : Housekeeping - remove aged utilisation_stats data.
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

BEGIN
	DELETE 
	FROM juror_mod.utilisation_stats us
	WHERE us.month_start < CURRENT_DATE - p_max_threshold;

    EXCEPTION

    	WHEN OTHERS THEN

        GET STACKED DIAGNOSTICS v_text_var1 = MESSAGE_TEXT,
                                v_text_var2 = PG_EXCEPTION_DETAIL,
                                v_text_var3 = PG_EXCEPTION_HINT;

        p_print_msg := 'DELETE FAILED - ERROR:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

        RAISE NOTICE '%', p_print_msg;	
END;
$$
