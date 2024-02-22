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


CREATE TABLE juror_mod.financial_audit_details_appearances
(
    financial_audit_id int8       NOT NULL,
    juror_number       varchar(9) NOT NULL,
    attendance_date    date       NOT NULL,
    loc_code           varchar(3) NOT NULL,
    appearance_version int8       NOT NULL,

    CONSTRAINT financial_audit_details_appearances_pkey PRIMARY KEY (financial_audit_id, juror_number,
                                                                     attendance_date, loc_code,
                                                                     appearance_version),
    CONSTRAINT financial_audit_details_appearances_financial_audit_id_fk FOREIGN KEY (financial_audit_id) REFERENCES
        juror_mod.financial_audit_details (id),
    CONSTRAINT financial_audit_details_appearances_app_loc_code_fk FOREIGN KEY (loc_code) REFERENCES juror_mod
        .court_location (loc_code),
    CONSTRAINT financial_audit_details_appearances_appearance_juror_fk FOREIGN KEY (juror_number) REFERENCES
        juror_mod.juror (juror_number)
);

ALTER TABLE juror_mod.appearance
    ADD COLUMN version bigint NOT NULL default 1;