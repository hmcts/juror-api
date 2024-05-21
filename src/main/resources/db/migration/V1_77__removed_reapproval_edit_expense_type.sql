update juror_mod.financial_audit_details set type = 'APPROVED_EDIT'
where type ='REAPPROVED_EDIT';

ALTER TABLE juror_mod.financial_audit_details
    DROP CONSTRAINT financial_audit_details_type_check,
    ADD CONSTRAINT financial_audit_details_type_check CHECK (((type)::text = ANY
                                                              (ARRAY [('FOR_APPROVAL'::character varying)::text,
                                                                  ('APPROVED_BACS'::character varying)::text,
                                                                  ('APPROVED_CASH'::character varying)::text,
                                                                  ('REAPPROVED_CASH'::character varying)::text,
                                                                  ('REAPPROVED_BACS'::character varying)::text,
                                                                  ('FOR_APPROVAL_EDIT'::character varying)::text,
                                                                  ('APPROVED_EDIT'::character varying)::text])));