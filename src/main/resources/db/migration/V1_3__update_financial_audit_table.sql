ALTER TABLE juror_mod.financial_audit_details
    ADD COLUMN juror_revision bigint NOT NULL default -1;
ALTER TABLE juror_mod.financial_audit_details
    ADD CONSTRAINT financial_audit_details_fk_revision_number_juror
        FOREIGN KEY (juror_revision) REFERENCES juror_mod.rev_info (revision_number);

ALTER TABLE juror_mod.financial_audit_details
    ADD COLUMN court_location_revision bigint NULL;
ALTER TABLE juror_mod.financial_audit_details
    ADD CONSTRAINT financial_audit_details_fk_revision_number_court_location
        FOREIGN KEY (court_location_revision) REFERENCES juror_mod.rev_info (revision_number);



ALTER TABLE juror_mod.financial_audit_details
    ADD COLUMN type varchar(20) NOT NULL default 'UNKNOWN';
ALTER TABLE juror_mod.financial_audit_details
    ADD CONSTRAINT financial_audit_details_type_check CHECK (((type)::text = ANY (
        (ARRAY ['FOR_APPROVAL'::character varying, 'APPROVED'::character varying, 'EDIT'::character varying])::text[])));


ALTER TABLE juror_mod.financial_audit_details
    ADD COLUMN created_by varchar(20) NOT NULL default -1;
ALTER TABLE juror_mod.financial_audit_details
    ADD CONSTRAINT financial_audit_details_fk_created_by
        FOREIGN KEY (created_by) REFERENCES juror_mod.users (username);
ALTER TABLE juror_mod.financial_audit_details
    ADD COLUMN created_on timestamp(6) NOT NULL default '2000-01-01 00:00:00';

ALTER TABLE juror_mod.financial_audit_details
    DROP COLUMN submitted_by;
ALTER TABLE juror_mod.financial_audit_details
    DROP COLUMN submitted_on;
ALTER TABLE juror_mod.financial_audit_details
    DROP COLUMN approved_by;
ALTER TABLE juror_mod.financial_audit_details
    DROP COLUMN approved_on;
ALTER TABLE juror_mod.financial_audit_details
    DROP COLUMN juror_revision_when_approved;