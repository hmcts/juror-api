ALTER TABLE juror_mod.juror
    ADD COLUMN claiming_substance_allowance bool NOT NULL DEFAULT false;
ALTER TABLE juror_mod.juror
    RENAME COLUMN smart_card to smart_card_number;
ALTER TABLE juror_mod.juror
    DROP COLUMN amount_spent;

ALTER TABLE juror_mod.juror_audit
    ADD COLUMN claiming_substance_allowance bool NOT NULL DEFAULT false;
ALTER TABLE juror_mod.juror_audit
    ADD COLUMN smart_card_number varchar(20) NULL;
