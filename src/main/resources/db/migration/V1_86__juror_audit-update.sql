ALTER TABLE juror_mod.juror
    add column last_modified_by varchar(20);

ALTER TABLE juror_mod.juror_audit
    add column last_modified_by varchar(20),
    add column last_update      timestamp;

CREATE INDEX juror_audit_juror_number_idx ON juror_mod.juror_audit (juror_number, last_update);
CREATE INDEX juror_audit_last_update_idx ON juror_mod.juror_audit (last_update, juror_number);

CREATE INDEX juror_trial_juror_number_idx ON juror_mod.juror_trial USING btree (juror_number);
drop index juror_mod.juror_trial_loc_code_idx;
CREATE INDEX juror_trial_loc_code_idx ON juror_mod.juror_trial USING btree (loc_code, juror_number);