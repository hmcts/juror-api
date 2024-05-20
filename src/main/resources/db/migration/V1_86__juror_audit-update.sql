ALTER TABLE juror_mod.rev_info
    add column changed_by varchar(20);

CREATE INDEX rev_info_revision_timestamp_idx ON juror_mod.rev_info (revision_timestamp,revision_number);

CREATE INDEX juror_trial_juror_number_idx ON juror_mod.juror_trial USING btree (juror_number);
drop index juror_mod.juror_trial_loc_code_idx;
CREATE INDEX juror_trial_loc_code_idx ON juror_mod.juror_trial USING btree (loc_code, juror_number);
CREATE INDEX juror_pool_status_idx ON juror_mod.juror_pool (status, owner, is_active);

