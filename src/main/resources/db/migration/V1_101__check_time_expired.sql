CREATE OR REPLACE FUNCTION juror_mod.check_time_expired(start_time_int INTEGER, max_timeout INTEGER)
RETURNS BOOLEAN
LANGUAGE plpgsql
as
/***********************************************************************************************************************
*  Author  : Andrew Fraser
*  Created : 12 Feb 2024
*  Purpose : Housekeeping - compares the current time to the maximum timeout threshold, if reached, log an error and exit.
* 
*   Change History:
*
*   Ver  Date     Author     Description
*   ---  ----     ------     -----------
*
***********************************************************************************************************************/

$$
DECLARE 
	print_msg text;
	curr_time timestamp;
	curr_time_int INTEGER;
BEGIN
	curr_time := now();
	curr_time_int := EXTRACT(EPOCH FROM curr_time)/60;
	
	IF (curr_time_int - start_time_int) > max_timeout THEN
		print_msg = '*** TIME EXPIRED AT '||TO_CHAR(curr_time,'dd-Mon-yyyy hh24:mi');
		CALL juror_mod.hk_insert_audit('-1', curr_time, print_msg);
		RETURN true;
	ELSE
		RETURN false;
	END IF; 

END;
$$ 
