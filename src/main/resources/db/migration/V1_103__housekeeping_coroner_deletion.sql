CREATE OR REPLACE PROCEDURE juror_mod.housekeeping_coroner_deletion(p_max_threshold INTEGER, p_start_time_int INTEGER, p_max_timeout INTEGER, INOUT p_print_msg text)
LANGUAGE plpgsql
AS
/***********************************************************************************************************************
*  Author  : Andrew Fraser
*  Created : 23 Feb 2024
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

	<<Deletes>>
	BEGIN
		-- child records
	 	WITH aged_coroners
	 	AS
	 	(
	 		SELECT cp.cor_pool_no
	        FROM juror_mod.coroner_pool cp
	        WHERE cp.cor_service_dt < CURRENT_DATE - p_max_threshold
	    )
		DELETE 
		FROM juror_mod.coroner_pool_detail cpd 
		USING aged_coroners ac
		WHERE cpd.cor_pool_no = ac.cor_pool_no;
		
		-- check if timeout has elapsed - if so exit the process
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;
	
		DELETE 
		FROM juror_mod.coroner_pool cp 
		WHERE cp.cor_service_dt < CURRENT_DATE - p_max_threshold;
	
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
			
	        p_print_msg := 'ERROR:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;
	
	        RAISE NOTICE '%', p_print_msg;	
	END;       
END;
$$
