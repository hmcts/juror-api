--Depends on migration 1_78
ALTER table juror_mod.financial_audit_details
    add CONSTRAINT financial_audit_details_id_loc_code_unique UNIQUE (id, loc_code)
;


ALTER TABLE juror_mod.financial_audit_details_appearances
    add column  last_approved_faudit bigint,
    add CONSTRAINT financial_audit_details_appearances_last_fAudit_fk
        FOREIGN KEY (last_approved_faudit, loc_code) REFERENCES juror_mod.financial_audit_details (id, loc_code)
;

CREATE INDEX appearance_audit_loc_code_idx ON juror_mod.appearance_audit (loc_code, juror_number, attendance_date,
                                                                          attendance_audit_number, "version");
CREATE INDEX appearance_audit_f_audit_loc_code_idx ON juror_mod.appearance_audit (f_audit_loc_code, juror_number, attendance_date,
                                                                          attendance_audit_number, "version");

CREATE OR REPLACE VIEW juror_mod.low_level_financial_audit_details
AS
select a3.*,
       a3.total_due - a3.total_paid as total_outstanding
from (select a2.*,
             a2.total_travel_due - a2.total_travel_paid                 as total_travel_outstanding,
             a2.total_financial_loss_due - a2.total_financial_loss_paid as total_financial_loss_outstanding,
             a2.total_subsistence_due - a2.total_subsistence_paid       as total_subsistence_outstanding,
             (case
                  when (a2.total_smart_card_paid - a2.total_smart_card_due) >= 0
                      then a2.total_smart_card_paid - a2.total_smart_card_due
                  else a2.total_smart_card_due
                 end)                                                   as total_smartcard_outstanding,
             (a2.total_travel_due + a2.total_financial_loss_due + a2.total_subsistence_due) -
             a2.total_smart_card_due                                    as total_due,
             (a2.total_travel_paid + a2.total_financial_loss_paid + a2.total_subsistence_paid) -
             a2.total_smart_card_paid                                   as total_paid

      from (select fad.*,
                   aa.attendance_date,
                   aa.pool_number,
                   aa.trial_number,
                   aa.appearance_stage,
                   aa.attendance_type,
                   aa.is_draft_expense,
                   aa.pay_cash,
                   aa.f_audit,
                   aa.attendance_audit_number,
                   ja.first_name,
                   ja.last_name,

                   COALESCE(aa.public_transport_total_due, 0::numeric) +
                   COALESCE(aa.hired_vehicle_total_due, 0::numeric) +
                   COALESCE(aa.motorcycle_total_due, 0::numeric) +
                   COALESCE(aa.car_total_due, 0::numeric) +
                   COALESCE(aa.pedal_cycle_total_due, 0::numeric) +
                   COALESCE(aa.parking_total_due, 0::numeric)  AS total_travel_due,

                   COALESCE(aa.public_transport_total_paid, 0::numeric) +
                   COALESCE(aa.hired_vehicle_total_paid, 0::numeric) +
                   COALESCE(aa.motorcycle_total_paid, 0::numeric) +
                   COALESCE(aa.car_total_paid, 0::numeric) +
                   COALESCE(aa.pedal_cycle_total_paid, 0::numeric) +
                   COALESCE(aa.parking_total_paid, 0::numeric) AS total_travel_paid,

                   COALESCE(aa.loss_of_earnings_due, 0::numeric) +
                   COALESCE(aa.childcare_total_due, 0::numeric) +
                   COALESCE(aa.misc_total_due, 0::numeric)     AS total_financial_loss_due,

                   COALESCE(aa.loss_of_earnings_paid, 0::numeric) +
                   COALESCE(aa.childcare_total_paid, 0::numeric) +
                   COALESCE(aa.misc_total_paid, 0::numeric)    AS total_financial_loss_paid,

                   COALESCE(aa.subsistence_due, 0::numeric)    AS total_subsistence_due,
                   COALESCE(aa.subsistence_paid, 0::numeric)   AS total_subsistence_paid,
                   COALESCE(aa.smart_card_due, 0::numeric)     AS total_smart_card_due,
                   COALESCE(aa.smart_card_paid, 0::numeric)    AS total_smart_card_paid

            from juror_mod.financial_audit_details fad
                     join juror_mod.financial_audit_details_appearances fada
                          on fada.financial_audit_id = fad.id and fada.loc_code = fad.loc_code
                     join juror_mod.appearance_audit aa
                          on aa.f_audit_loc_code = fada.loc_code and aa.attendance_date = fada.attendance_date and
                             aa.juror_number = fad.juror_number
                              and aa."version" = fada.appearance_version
                     join juror_mod.juror_audit ja
                          on ja.revision = fad.juror_revision and ja.juror_number = fad.juror_number) a2) a3;
;