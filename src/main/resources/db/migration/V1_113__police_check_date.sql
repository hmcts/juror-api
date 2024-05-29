alter table juror_mod.juror
    ADD COLUMN police_check_last_update timestamp(6) NULL;
CREATE INDEX juror_police_check_last_update_idx ON juror_mod.juror (police_check_last_update,police_check);