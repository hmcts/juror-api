ALTER TABLE juror_mod.appearance_audit
    ADD COLUMN version bigint default 1 not null;


UPDATE juror_mod.juror
    SET police_check = 'NOT_CHECKED'
    WHERE police_check IS NULL;
ALTER TABLE juror_mod.juror
    ALTER COLUMN police_check SET DEFAULT 'NOT_CHECKED',
    ALTER COLUMN police_check SET NOT NULL;

ALTER TABLE juror_mod.financial_audit_details
    ADD COLUMN juror_number varchar(9) NOT NULL default 'ERROR',
    ADD COLUMN loc_code     varchar(3) NOT NULL default 'ERR',
    ADD CONSTRAINT financial_audit_details_appearance_juror_fk FOREIGN KEY (juror_number) REFERENCES
        juror_mod.juror (juror_number),
    ADD CONSTRAINT financial_audit_details_app_loc_code_fk FOREIGN KEY (loc_code) REFERENCES juror_mod.court_location
        (loc_code),
    DROP CONSTRAINT financial_audit_details_type_check,
    ADD CONSTRAINT financial_audit_details_type_check CHECK (((type)::text = ANY
                                                              (ARRAY [('FOR_APPROVAL'::character varying)::text,
                                                                  ('APPROVED_BACS'::character varying)::text,
                                                                  ('APPROVED_CASH'::character varying)::text,
                                                                  ('REAPPROVED_CASH'::character varying)::text,
                                                                  ('REAPPROVED_BACS'::character varying)::text,
                                                                  ('FOR_APPROVAL_EDIT'::character varying)::text,
                                                                  ('APPROVED_EDIT'::character varying)::text,
                                                                  ('REAPPROVED_EDIT'::character varying)::text])));



ALTER TABLE juror_mod.financial_audit_details_appearances
    DROP CONSTRAINT financial_audit_details_appearances_pkey,
    DROP COLUMN juror_number,
    DROP COLUMN loc_code,
    ADD CONSTRAINT financial_audit_details_appearances_pool_fk FOREIGN KEY (pool_number)
        REFERENCES juror_mod.pool (pool_no),
    ADD COLUMN pool_number varchar(9) NOT NULL default 'ERROR';
ALTER TABLE juror_mod.financial_audit_details_appearances
    ADD CONSTRAINT financial_audit_details_appearances_pkey PRIMARY KEY (financial_audit_id, attendance_date, appearance_version);

ALTER TABLE juror_mod.appearance_audit
    ALTER COLUMN version drop not null;