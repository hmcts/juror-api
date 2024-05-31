CREATE OR REPLACE PROCEDURE juror_mod.hk_insert_log(p_start_time TIMESTAMP,p_rows_deleted INTEGER,p_rows_in_error INTEGER)
LANGUAGE plpgsql
as
/***********************************************************************************************************************
*  Author  : Andrew Fraser
*  Created : 12 Feb 2024
*  Purpose : Housekeeping - logs the housekeeping process event.
* 
*   Change History:
*
*   Ver  Date     Author     Description
*   ---  ----     ------     -----------
*
***********************************************************************************************************************/

$$
BEGIN
	INSERT INTO juror_mod.hk_run_log(start_time,end_time,jurors_deleted,jurors_error)
	VALUES (p_start_time,CURRENT_TIMESTAMP,p_rows_deleted,p_rows_in_error);
END;
$$

