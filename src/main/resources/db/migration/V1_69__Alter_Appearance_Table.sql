ALTER TABLE juror_mod.appearance_audit
RENAME COLUMN pool_trial_no TO trial_number;

ALTER TABLE juror_mod.appearance
RENAME COLUMN pool_trial_no TO trial_number;

ALTER TABLE juror_mod.appearance ADD CONSTRAINT appearance_juror_fk FOREIGN KEY (juror_number)
REFERENCES juror_mod.juror(juror_number);

ALTER TABLE juror_mod.appearance ADD CONSTRAINT appearance_pool_fk FOREIGN KEY (pool_number)
REFERENCES juror_mod.pool(pool_no);

ALTER TABLE juror_mod.appearance ADD CONSTRAINT appearance_trial_fk FOREIGN KEY (trial_number, loc_code)
REFERENCES juror_mod.trial(trial_number, loc_code);
