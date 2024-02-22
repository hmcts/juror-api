ALTER TABLE juror_mod.appearance
    DROP COLUMN payment_approved_date;

ALTER TABLE juror_mod.appearance
    DROP COLUMN expense_submitted_date;

ALTER TABLE juror_mod.appearance
    DROP COLUMN mileage_due;
ALTER TABLE juror_mod.appearance
    DROP COLUMN mileage_paid;

ALTER TABLE juror_mod.appearance_audit
    DROP COLUMN payment_approved_date;

ALTER TABLE juror_mod.appearance_audit
    DROP COLUMN expense_submitted_date;

ALTER TABLE juror_mod.appearance_audit
    DROP COLUMN mileage_due;
ALTER TABLE juror_mod.appearance_audit
    DROP COLUMN mileage_paid;


ALTER TABLE juror_mod.users
    ADD COLUMN approval_limit numeric(8, 2) NOT NULL default 0;

ALTER TABLE juror_mod.users
    ADD COLUMN can_approve bool NOT NULL default false;

ALTER TABLE juror_mod.court_location
    ADD COLUMN cost_centre varchar(9) NOT NULL default 'none';
ALTER TABLE juror_mod.financial_audit_details
    DROP CONSTRAINT financial_audit_details_type_check;
ALTER TABLE juror_mod.financial_audit_details
    ADD CONSTRAINT financial_audit_details_type_check CHECK (((type)::text = ANY (
        (ARRAY ['FOR_APPROVAL'::character varying, 'APPROVED_BACS'::character varying,
            'APPROVED_CASH'::character varying, 'EDIT'::character varying])::text[])));

