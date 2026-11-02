-- JS-1188 Fix issue with housekeeping timeout checks

CREATE OR REPLACE PROCEDURE juror_mod.housekeeping_process(IN p_max_timeout integer DEFAULT 600, IN p_owner_restrict boolean DEFAULT false)
 LANGUAGE plpgsql
AS $procedure$
DECLARE
temprow RECORD;
  	v_print_msg TEXT;
    v_start_time TIMESTAMP := clock_timestamp()::timestamp;
   	v_start_time_int INTEGER;
    v_rows_deleted INTEGER := 0;
    v_rows_in_error INTEGER := 0;
   	v_row_limit INTEGER;
    v_audit_threshold INTEGER;
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
							WHERE hp.key = 1
						);

	v_row_limit :=  (
						SELECT hp.value::INTEGER
						FROM juror_mod.hk_params hp
						WHERE hp."key" = 3	-- Maximum Pool deletions allowed/check for juror
					);

<<Main>>
BEGIN

		raise notice 'Start :%',v_start_time;

FOR temprow IN
	       /*
			* create a loop and, for each juror number, delete the assocated rows
			*/
SELECT  jh.juror_number
FROM
  (
    SELECT  jh.juror_number,
            max(jh.date_created) as date_created
    FROM juror_mod.juror_history jh
    GROUP BY jh.juror_number
  ) jh
    JOIN
  (
    SELECT 	jp.juror_number,
            jp.owner
    FROM juror_mod.juror_pool jp -- link to identify the owner associated
    WHERE p_owner_restrict = false
       OR
      (	p_owner_restrict = true
        AND
         NOT EXISTS(
           SELECT 1
           FROM juror_mod.hk_owner_restrict hor
           WHERE hor.id = jp.owner::INTEGER
             AND hor.value = 'NO'
         )
        )
  ) jp
  ON jh.juror_number = jp.juror_number
WHERE jh.date_created < CURRENT_DATE - v_max_threshold
  LIMIT v_row_limit

	    LOOP
	        CALL juror_mod.housekeeping_juror_deletion(temprow.juror_number,v_start_time_int,p_max_timeout,v_print_msg);

-- log if the deletion was successful or not based on the return value of the call
IF (v_print_msg IS NULL) THEN
	   			CALL juror_mod.hk_insert_audit(temprow.juror_number, clock_timestamp()::timestamp, 'Deleted Juror');
				v_rows_deleted := v_rows_deleted + 1;
			ELSIF POSITION('TIMED' IN v_print_msg) > 0 THEN
				EXIT;
ELSE
		        CALL juror_mod.hk_insert_audit(temprow.juror_number, clock_timestamp()::timestamp, v_print_msg);
				v_rows_in_error := v_rows_in_error + 1;
END IF;

COMMIT;  -- COMMIT THE DELETES FOR THIS JUROR
END LOOP;


		raise notice 'Juror rows_deleted :%',v_rows_deleted;
		raise notice 'Juror rows_in_error :%',v_rows_in_error;

		-- write to log that the run has completed
CALL juror_mod.hk_insert_log(v_start_time,v_rows_deleted,v_rows_in_error);

IF POSITION('TIMED' IN v_print_msg) > 0 THEN
			EXIT Main;
END if;

	    -- delete parent records --

		-- Trials
CALL juror_mod.housekeeping_trial_deletion(v_max_threshold,v_start_time_int,p_max_timeout,v_print_msg);
IF POSITION('TIMED' IN v_print_msg) > 0 THEN
			EXIT Main;
END if;


		-- Coroners
CALL juror_mod.housekeeping_coroner_deletion(v_max_threshold,v_start_time_int,p_max_timeout,v_print_msg);
IF POSITION('TIMED' IN v_print_msg) > 0 THEN
			EXIT Main;
END if;

		-- Pools
CALL juror_mod.housekeeping_pool_deletion(v_max_threshold,v_start_time_int,p_max_timeout,v_print_msg);
IF POSITION('TIMED' IN v_print_msg) > 0 THEN
			EXIT Main;
END if;


		-- Standalone tables --
		-- Logs
CALL juror_mod.housekeeping_log_deletion(v_max_threshold,v_start_time_int,p_max_timeout,v_print_msg);
IF POSITION('TIMED' IN v_print_msg) > 0 THEN
			EXIT Main;
END if;


		-- Holidays
CALL juror_mod.housekeeping_holiday_deletion(v_max_threshold,v_start_time_int,p_max_timeout,v_print_msg);
IF POSITION('TIMED' IN v_print_msg) > 0 THEN
			EXIT Main;
END if;

		-- Content store
CALL juror_mod.housekeeping_content_store_deletion(v_max_threshold,v_start_time_int,p_max_timeout,v_print_msg);
IF POSITION('TIMED' IN v_print_msg) > 0 THEN
			EXIT Main;
END if;

		-- Utilisation stats
CALL juror_mod.housekeeping_utilisation_stats_deletion(v_max_threshold,v_start_time_int,p_max_timeout,v_print_msg);
IF POSITION('TIMED' IN v_print_msg) > 0 THEN
			EXIT Main;
END if;

COMMIT;

raise notice 'End :%', now();


		-- write to log that the run has completed
CALL juror_mod.hk_insert_log(v_start_time,v_rows_deleted,v_rows_in_error);

END;
END;
$procedure$
;


CREATE OR REPLACE PROCEDURE juror_mod.housekeeping_digital_process(IN p_max_timeout integer DEFAULT 600)
 LANGUAGE plpgsql
AS $procedure$
DECLARE
temprow RECORD;
  	v_print_msg TEXT;
    v_start_time TIMESTAMP := clock_timestamp()::timestamp;
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
SELECT  jr.juror_number, jr.reply_type
FROM juror_mod.juror_response jr
WHERE jr.completed_at < (CURRENT_DATE - v_max_threshold)
  LIMIT v_row_limit

        LOOP

	        CALL juror_mod.housekeeping_juror_digital_deletion(temprow.juror_number,v_max_threshold,v_start_time_int,p_max_timeout,v_print_msg);

-- log if the deletion was successful or not based on the return value of the call
IF (v_print_msg IS NULL) THEN
	   			CALL juror_mod.hk_insert_audit(temprow.juror_number, clock_timestamp()::timestamp, 'Deleted '||temprow.reply_type||' Response');
				v_rows_deleted := v_rows_deleted + 1;
			ELSIF POSITION('TIMED' IN v_print_msg) > 0 THEN
				EXIT;
ELSE
		        CALL juror_mod.hk_insert_audit(temprow.juror_number, clock_timestamp()::timestamp, v_print_msg);
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
$procedure$
;


-- DROP PROCEDURE juror_mod.housekeeping_juror_digital_deletion(in varchar, in int4, in int4, in int4, inout text);

CREATE OR REPLACE PROCEDURE juror_mod.housekeeping_juror_digital_deletion(IN p_juror_number character varying, IN p_threshold integer, IN p_start_time_int integer, IN p_max_timeout integer, INOUT p_print_msg text)
 LANGUAGE plpgsql
AS $procedure$
DECLARE
v_text_var1 TEXT;
   	v_text_var2 TEXT;
   	v_text_var3 TEXT;
   	v_timed_out BOOLEAN;

BEGIN
	-- Perform the deletion
  <<Deletes>>
BEGIN

p_print_msg := NULL;

DELETE FROM juror_mod.juror_reasonable_adjustment jra WHERE jra.juror_number = p_juror_number;
-- check if timeout has elapsed - if so exit loop
SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
IF v_timed_out THEN
			p_print_msg := 'DELETE FAILED - ERROR:-> TIMED OUT';
   		EXIT Deletes;
END IF;

DELETE FROM juror_mod.juror_response_aud jra WHERE jra.juror_number = p_juror_number;
-- check if timeout has elapsed - if so exit loop
SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
IF v_timed_out THEN
			p_print_msg := 'DELETE FAILED - ERROR:-> TIMED OUT';
      EXIT Deletes;
END IF;

DELETE FROM juror_mod.juror_response_cjs_employment jrce WHERE jrce.juror_number = p_juror_number;
-- check if timeout has elapsed - if so exit loop
SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
IF v_timed_out THEN
			p_print_msg := 'DELETE FAILED - ERROR:-> TIMED OUT';
      EXIT Deletes;
END IF;

DELETE FROM juror_mod.user_juror_response_audit ujra WHERE ujra.juror_number = p_juror_number;
-- check if timeout has elapsed - if so exit loop
SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
IF v_timed_out THEN
			p_print_msg := 'DELETE FAILED - ERROR:-> TIMED OUT';
      EXIT Deletes;
END IF;

DELETE FROM juror_mod.juror_response jr WHERE jr.juror_number = p_juror_number;
-- check if timeout has elapsed - if so exit loop
SELECT juror_mod.check_time_expired(p_start_time_int,p_max_timeout) INTO v_timed_out;
IF v_timed_out THEN
			p_print_msg := 'DELETE FAILED - ERROR:-> TIMED OUT';
      EXIT Deletes;
END IF;

		-- upon error...
EXCEPTION

        	WHEN OTHERS THEN

	        GET STACKED DIAGNOSTICS v_text_var1 = MESSAGE_TEXT,
	                                v_text_var2 = PG_EXCEPTION_DETAIL,
	                                v_text_var3 = PG_EXCEPTION_HINT;

	        p_print_msg := 'DELETE FAILED - ERROR:->' || v_text_var1 || '|' || v_text_var2 || '|' || v_text_var3;

END;
END;
$procedure$
;


CREATE OR REPLACE FUNCTION juror_mod.check_time_expired(start_time_int integer, max_timeout integer)
 RETURNS boolean
 LANGUAGE plpgsql
AS $function$
DECLARE
print_msg text;
	curr_time timestamp;
	curr_time_int INTEGER;
BEGIN
	curr_time := clock_timestamp()::timestamp;
	curr_time_int := EXTRACT(EPOCH FROM curr_time)/60;

	IF (curr_time_int - start_time_int) > max_timeout THEN
		print_msg = '*** TIME EXPIRED AT '||TO_CHAR(curr_time,'dd-Mon-yyyy hh24:mi');
CALL juror_mod.hk_insert_audit('-1', curr_time, print_msg);
RETURN true;
ELSE
		RETURN false;
END IF;

END;
$function$
;
