

-- Settings apply to this transaction only.
-- lock_timeout: fail fast rather than queue behind a long-running transaction. A queued
--   SHARE lock request would block every later writer AND reader behind it.
-- statement_timeout: disable any server/role default so the large builds aren't killed.
-- maintenance_work_mem: speeds up index builds; size to the server's available memory.
SET LOCAL lock_timeout = '30s';
SET LOCAL statement_timeout = 0;
SET LOCAL maintenance_work_mem = '1GB';


-- -------------------------------------------------------------------------------------
--    payment_data (~1GB)
-- -------------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS payment_data_juror_number_idx
    ON juror_mod.payment_data USING btree (juror_number);


-- -------------------------------------------------------------------------------------
--    financial_audit_details (>1GB)
-- -------------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS financial_audit_details_juror_number_idx
    ON juror_mod.financial_audit_details USING btree (juror_number);


-- -------------------------------------------------------------------------------------
--    financial_audit_details_appearances (>3GB)
-- -------------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS financial_audit_details_appearances_last_faudit_idx
    ON juror_mod.financial_audit_details_appearances USING btree (last_approved_faudit, loc_code)
    WHERE last_approved_faudit IS NOT NULL;


-- -------------------------------------------------------------------------------------
--    appearance (>5GB)
-- -------------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS appearance_pool_number_idx
    ON juror_mod.appearance USING btree (pool_number)
    WHERE pool_number IS NOT NULL;


-- -------------------------------------------------------------------------------------
--    appearance (>5GB)
-- -------------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS appearance_trial_number_idx
    ON juror_mod.appearance USING btree (trial_number, loc_code)
    WHERE trial_number IS NOT NULL;


-- -------------------------------------------------------------------------------------
--    appearance_audit (>10GB)
-- -------------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS appearance_audit_juror_number_idx
    ON juror_mod.appearance_audit USING btree (juror_number);

-- -------------------------------------------------------------------------------------
--    appearance_audit (>10GB)
-- -------------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS appearance_audit_f_audit_idx
    ON juror_mod.appearance_audit USING btree (f_audit, loc_code)
    WHERE f_audit IS NOT NULL;
