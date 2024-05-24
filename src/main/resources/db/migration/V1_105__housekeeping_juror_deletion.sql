CREATE OR REPLACE PROCEDURE juror_mod.housekeeping_juror_deletion(p_juror_number VARCHAR(9), p_start_time_int INTEGER, p_max_timeout INTEGER, INOUT p_print_msg text)
LANGUAGE plpgsql
AS
/***********************************************************************************************************************
*  Author  : Andrew Fraser
*  Created : 12 Feb 2024
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
   	audit_rows 	RECORD;
	financial_audit_rows RECORD;

BEGIN
    p_print_msg := NULL; -- reset 

    CREATE TEMPORARY TABLE IF NOT EXISTS deleted_appearance_audit(revision INTEGER);
    CREATE TEMPORARY TABLE IF NOT EXISTS deleted_juror_audit (revision INTEGER);
    CREATE TEMPORARY TABLE IF NOT EXISTS deleted_financial_audit (financial_audit_id INTEGER);
   
    <<Deletes>>
	-- Perform the deletion 
	BEGIN
		-- Juror child records
		DELETE FROM juror_mod.bulk_print_data bpd WHERE bpd.juror_no = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;
	
		DELETE FROM juror_mod.payment_data pd WHERE pd.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		WITH deleted
		AS
		(
			DELETE 
			FROM juror_mod.financial_audit_details_appearances fada 
			WHERE fada.juror_number = p_juror_number
			RETURNING fada.financial_audit_id
		)
		INSERT INTO deleted_financial_audit (financial_audit_id)
		SELECT d.financial_audit_id
		FROM deleted d;
	
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		DELETE FROM juror_mod.message m WHERE m.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		WITH deleted
		AS
		(
			DELETE 
			FROM juror_mod.appearance_audit aa 
			WHERE aa.juror_number = p_juror_number
			RETURNING aa.revision
		)
		INSERT INTO deleted_appearance_audit (revision)
		SELECT d.revision
		FROM deleted d;

		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		DELETE FROM juror_mod.appearance a WHERE a.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		DELETE FROM juror_mod.contact_log cl WHERE cl.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		WITH deleted
		AS
		(
			DELETE 
			FROM juror_mod.juror_audit ja 
			WHERE ja.juror_number = p_juror_number 
			RETURNING ja.revision
		)
		INSERT INTO deleted_juror_audit (revision)
		SELECT d.revision
		FROM deleted d;

		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		DELETE FROM juror_mod.pending_juror pj WHERE pj.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		DELETE FROM juror_mod.juror_pool jp WHERE jp.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		DELETE FROM juror_mod.juror_trial t WHERE t.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		DELETE FROM juror_mod.juror_history jh WHERE jh.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;
	
		-- Juror parent record
		DELETE FROM juror_mod.juror j WHERE j.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
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
			ROLLBACK;
	        p_print_msg := 'DELETE FAILED - ERROR:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;
	       
	END;

	-- clean up the sequence revision numbers
	DELETE 
	FROM juror_mod.rev_info ri
	USING deleted_appearance_audit daa
	WHERE ri.revision_number = daa.revision;

	DELETE 
	FROM juror_mod.rev_info ri
	USING deleted_juror_audit dja
	WHERE ri.revision_number = dja.revision;

	-- clean up the financial audit details 
	DELETE	
	FROM juror_mod.financial_audit_details_appearances fada
	USING deleted_financial_audit dfa
	WHERE fada.financial_audit_id = dfa.financial_audit_id;

	-- Remove the temporary tables
    DROP TABLE IF EXISTS deleted_appearance_audit;
    DROP TABLE IF EXISTS deleted_juror_audit;
    DROP TABLE IF EXISTS deleted_financial_audit;
   

END;
$$

