CREATE OR REPLACE PROCEDURE juror_mod.housekeeping_digital_process(p_max_timeout INTEGER DEFAULT 600)
LANGUAGE plpgsql
AS
/***********************************************************************************************************************
*  Author  : Andrew Fraser
*  Created : 16 Feb 2024
*  Purpose : Housekeeping for Juror responses parent and child records.
* 
* 	step 1	- Identify all aged juror responses - taking the last updated rows as the date to compare with.
* 	step 2	- Loop through a juror record at a time
* 	step 3	- Perform deletes on child reocrds first and then the parent record
* 	step 4	- log event
* 
* 	N.B. p_max_timeout is used as a time limit check, if the max time is reached then exit the process - this stops the 
* 		 process overrunning and impacting BAU.
* 
*   Change History:
*
*   Ver  Date     Author     Description
*   ---  ----     ------     -----------
*
***********************************************************************************************************************/

$$
DECLARE
   	temprow RECORD;
  	v_print_msg TEXT;
    v_start_time TIMESTAMP := CURRENT_TIMESTAMP::timestamp;
   	v_start_time_int INTEGER;
    v_rows_deleted INTEGER := 0;
    v_rows_in_error INTEGER := 0;
   	v_row_limit INTEGER;
    v_text_var1 TEXT;
   	v_text_var2 TEXT;
   	v_text_var3 TEXT;
   	v_max_threshold INTEGER;
    
BEGIN
		
	-- store start time (as an integer) in order to compare to timeout expiry date whilst looping through deletes
	v_start_time_int := EXTRACT(EPOCH FROM v_start_time)/60;

	v_max_threshold :=	(
							SELECT hp.value::INTEGER
							FROM juror_mod.hk_params hp
							WHERE hp.key = 5
							and lower(hp.description) LIKE '%digital%'
						);

	v_row_limit :=  (
						SELECT hp.value::INTEGER
						FROM juror_mod.hk_params hp
						WHERE hp."key" = 3	-- Maximum Pool deletions allowed/check for juror
					);								
				
    FOR temprow IN 
       /*
		* create a loop and, for each juror number, delete the assocated rows
		*/
		SELECT  jr.juror_number
		FROM juror_mod.juror_response jr
        WHERE jr.completed_at < (CURRENT_DATE - v_max_threshold)
		LIMIT v_row_limit
	
        LOOP
	        
	        CALL juror_mod.housekeeping_juror_digital_deletion(temprow.juror_number,v_max_threshold,v_start_time_int,p_max_timeout,v_print_msg);
	       
	        -- log if the deletion was successful or not based on the return value of the call
	        IF (v_print_msg IS NULL) THEN
	   			CALL juror_mod.hk_insert_audit(temprow.juror_number, v_start_time, 'Deleted');
				v_rows_deleted := v_rows_deleted + 1;
			ELSIF POSITION('TIMED' IN v_print_msg) THEN
				EXIT;
			ELSE
		        CALL juror_mod.hk_insert_audit(temprow.juror_number, v_start_time, v_print_msg);
				v_rows_in_error := v_rows_in_error + 1;
		    END IF;
	
		    COMMIT;  -- COMMIT THE DELETES FOR THIS JUROR
	END LOOP;

	raise notice 'Start :%',v_start_time;
	raise notice 'rows_deleted :%',v_rows_deleted;
	raise notice 'rows_in_error :%',v_rows_in_error;

	-- write to log that the run has completed
	CALL juror_mod.hk_insert_log(v_start_time,v_rows_deleted,v_rows_in_error);

END;
$$
