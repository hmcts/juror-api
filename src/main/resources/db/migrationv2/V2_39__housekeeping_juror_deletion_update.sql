-- DROP PROCEDURE juror_mod.housekeeping_juror_deletion(in varchar, in int4, in int4, inout text);

CREATE OR REPLACE PROCEDURE juror_mod.housekeeping_juror_deletion(IN p_juror_number character varying, IN p_start_time_int integer, IN p_max_timeout integer, INOUT p_print_msg text)
 LANGUAGE plpgsql
AS $procedure$
DECLARE
    v_text_var1 TEXT;
   	v_text_var2 TEXT;
   	v_text_var3 TEXT;
   	v_timed_out BOOLEAN;
   	audit_rows 	RECORD;
	financial_audit_rows RECORD;

BEGIN
    p_print_msg := NULL; -- reset

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
			using juror_mod.financial_audit_details fad
			where fad.id = fada.financial_audit_id and fad.juror_number = p_juror_number
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


		DELETE FROM juror_mod.appearance_audit aa WHERE aa.juror_number = p_juror_number;
		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

	    -- must remove juror_mod.financial_audit_details before juror_mod.appearance
		if exists (
		   SELECT 1
			   FROM information_schema.tables
			   WHERE table_type = 'LOCAL TEMPORARY'
			   AND table_name = 'deleted_financial_audit'
			) then
				-- clean up the financial audit details
				DELETE
				FROM juror_mod.financial_audit_details fda
				USING deleted_financial_audit dfa
				WHERE fda.id = dfa.financial_audit_id;

				drop table deleted_financial_audit;
		end if;

		-- check if timeout has elapsed - if so exit loop
	    SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

        DELETE FROM juror_mod.bureau_snapshot bss WHERE bss.juror_number = p_juror_number;
        -- check if timeout has elapsed - if so exit loop
        SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
        IF v_timed_out THEN
            p_print_msg := 'ERROR:-> TIMED OUT';
            EXIT Deletes;
        END IF;

        DELETE FROM juror_mod.juror_third_party tp WHERE tp.juror_number = p_juror_number;
        -- check if timeout has elapsed - if so exit loop
        SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
        IF v_timed_out THEN
            p_print_msg := 'ERROR:-> TIMED OUT';
            EXIT Deletes;
        END IF;

        DELETE FROM juror_mod.juror_third_party_audit tpa WHERE tpa.juror_number = p_juror_number;
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

		DELETE FROM juror_mod.user_juror_response_audit ujra WHERE ujra.juror_number = p_juror_number;
        -- check if timeout has elapsed - if so exit loop
        SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
        IF v_timed_out THEN
            p_print_msg := 'DELETE FAILED - ERROR:-> TIMED OUT';
        END IF;


		DELETE FROM juror_mod.juror_audit ja WHERE ja.juror_number = p_juror_number;
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

		delete from juror_mod.juror_reasonable_adjustment ra where ra.juror_number = p_juror_number;
		SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		delete from juror_mod.juror_response_cjs_employment cjs where cjs.juror_number =  p_juror_number;
		SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

        delete from juror_mod.juror_response_aud jra where jra.juror_number = p_juror_number;
        SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
		IF v_timed_out THEN
			p_print_msg := 'ERROR:-> TIMED OUT';
			EXIT Deletes;
		END IF;

		delete from juror_mod.juror_response jr where jr.juror_number = p_juror_number;
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


END;
$procedure$
;
