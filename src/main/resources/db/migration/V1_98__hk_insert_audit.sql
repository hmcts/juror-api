CREATE OR REPLACE PROCEDURE juror_mod.hk_insert_audit(p_juror_number varchar(9), p_selected_date timestamp, p_deletion_summary text)
LANGUAGE plpgsql
AS
/***********************************************************************************************************************
*  Author  : Andrew Fraser
*  Created : 12 Feb 2024
*  Purpose : Housekeeping - used to create an audit trail for deletions and errors.
* 
*   Change History:
*
*   Ver  Date     Author     Description
*   ---  ----     ------     -----------
*
***********************************************************************************************************************/
$$
BEGIN
	-- log the juror details for an audit trial
     INSERT INTO juror_mod.hk_audit (juror_number, selected_date, deletion_date, deletion_summary)
     VALUES (p_juror_number, p_selected_date, CURRENT_TIMESTAMP, p_deletion_summary);

END;
$$

