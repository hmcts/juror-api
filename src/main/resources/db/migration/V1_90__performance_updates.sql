DROP INDEX juror_mod.juror_response_staff_login_idx;
CREATE INDEX juror_response_staff_login_idx ON juror_mod.juror_response (staff_login, processing_status, completed_at);
CREATE INDEX juror_pool_pool_number_idx ON juror_mod.juror_pool (pool_number,juror_number);

DROP index juror_mod.juror_response_last_name_idx
CREATE INDEX juror_response_last_name_idx ON juror_mod.juror_response (lower(last_name), date_received);
CREATE INDEX trial_loc_code_idx ON juror_mod.trial (loc_code,trial_start_date,trial_end_date);
CREATE INDEX juror_trial_loc_code_result_idx ON juror_mod.juror_trial (loc_code,"result",empanelled_date,return_date);

