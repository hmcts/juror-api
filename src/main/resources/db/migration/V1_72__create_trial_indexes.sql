CREATE INDEX juror_trial_loc_code_idx ON juror_mod.juror_trial USING btree (loc_code);

CREATE INDEX juror_trial_trial_number_idx ON juror_mod.juror_trial USING btree (trial_number);