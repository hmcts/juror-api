ALTER TABLE juror_mod.juror
    ADD COLUMN digital_by_default BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN dbd_preference VARCHAR(7),
    ADD CONSTRAINT juror_dbd_preference_val CHECK (dbd_preference IN ('Paper', 'Digital'));

ALTER TABLE juror_mod.juror_audit ADD COLUMN dbd_preference VARCHAR(7);
