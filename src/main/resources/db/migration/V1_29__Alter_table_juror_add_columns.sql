-- ALTER TABLE  juror_mod.juror DROP COLUMN IF EXISTS service_comp_comms_status
-- ALTER TABLE  juror_mod.juror DROP COLUMN IF EXISTS login_attemtps
-- ALTER TABLE  juror_mod.juror DROP COLUMN IF EXISTS is_locked

ALTER TABLE juror_mod.juror
    ADD COLUMN service_comp_comms_status VARCHAR(10) NULL;
ALTER TABLE juror_mod.juror
    ADD COLUMN login_attempts bigint NOT NULL default 0;
ALTER TABLE juror_mod.juror
    ADD COLUMN is_locked boolean not null default false;


