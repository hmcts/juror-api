--Depends on migration 1_78
ALTER table juror_mod.financial_audit_details
    add CONSTRAINT financial_audit_details_id_loc_code_unique UNIQUE (id, loc_code)
;


ALTER TABLE juror_mod.financial_audit_details_appearances
    add column last_approved_faudit bigint,
    add CONSTRAINT financial_audit_details_appearances_last_fAudit_fk
        FOREIGN KEY (last_approved_faudit, loc_code) REFERENCES juror_mod.financial_audit_details (id, loc_code)
;

--TODO add mapping logic for last approved

CREATE INDEX appearance_audit_loc_code_idx ON juror_mod.appearance_audit (loc_code, juror_number, attendance_date,
                                                                          attendance_audit_number, "version");
CREATE INDEX appearance_audit_f_audit_loc_code_idx ON juror_mod.appearance_audit (f_audit_loc_code, juror_number,
                                                                                  attendance_date,
                                                                                  attendance_audit_number, "version");

CREATE INDEX appearance_loc_code_idx ON juror_mod.appearance (loc_code, appearance_stage, is_draft_expense, juror_number);


CREATE OR REPLACE VIEW juror_mod.low_level_financial_audit_details
AS
select a4.*,
       a4.total_due - a4.total_paid as total_outstanding
from (select a3.*,
             a3.total_travel_due - a3.total_travel_paid                 as total_travel_outstanding,
             a3.total_financial_loss_due - a3.total_financial_loss_paid as total_financial_loss_outstanding,
             a3.total_subsistence_due - a3.total_subsistence_paid       as total_subsistence_outstanding,
             (case
                  when (a3.total_smart_card_paid - a3.total_smart_card_due) >= 0
                      then a3.total_smart_card_paid - a3.total_smart_card_due
                  else a3.total_smart_card_due
                 end)                                                   as total_smartcard_outstanding,
             (a3.total_travel_due + a3.total_financial_loss_due + a3.total_subsistence_due) -
             a3.total_smart_card_due                                    as total_due,
             (a3.total_travel_paid + a3.total_financial_loss_paid + a3.total_subsistence_paid) -
             a3.total_smart_card_paid                                   as total_paid

      from (select a2.*,
                   a2.public_transport_total_due +
                   a2.hired_vehicle_total_due +
                   a2.motorcycle_total_due +
                   a2.car_total_due +
                   a2.pedal_cycle_total_due +
                   a2.parking_total_due  AS total_travel_due,

                   a2.public_transport_total_paid +
                   a2.hired_vehicle_total_paid +
                   a2.motorcycle_total_paid +
                   a2.car_total_paid +
                   a2.pedal_cycle_total_paid +
                   a2.parking_total_paid AS total_travel_paid,

                   a2.loss_of_earnings_due +
                   a2.childcare_total_due +
                   a2.misc_total_due     AS total_financial_loss_due,

                   a2.loss_of_earnings_paid +
                   a2.childcare_total_paid +
                   a2.misc_total_paid    AS total_financial_loss_paid,

                   a2.subsistence_due    AS total_subsistence_due,
                   a2.subsistence_paid   AS total_subsistence_paid,
                   a2.smart_card_due     AS total_smart_card_due,
                   a2.smart_card_paid    AS total_smart_card_paid

            from (select fad.*,
                         fad.created_on::timestamp::date as created_on_date,
                         fada.last_approved_faudit,
                         aa.attendance_date,
                         aa.pool_number,
                         aa.trial_number,
                         aa.appearance_stage,
                         aa.attendance_type,
                         aa.is_draft_expense,
                         aa.pay_cash,
                         aa.f_audit,
                         aa.attendance_audit_number,

                         COALESCE(aa.public_transport_total_due, 0::numeric)  as public_transport_total_due,
                         COALESCE(aa.hired_vehicle_total_due, 0::numeric)     as hired_vehicle_total_due,
                         COALESCE(aa.motorcycle_total_due, 0::numeric)        as motorcycle_total_due,
                         COALESCE(aa.car_total_due, 0::numeric)               as car_total_due,
                         COALESCE(aa.pedal_cycle_total_due, 0::numeric)       as pedal_cycle_total_due,
                         COALESCE(aa.parking_total_due, 0::numeric)           as parking_total_due,
                         COALESCE(aa.loss_of_earnings_due, 0::numeric)        as loss_of_earnings_due,
                         COALESCE(aa.childcare_total_due, 0::numeric)         as childcare_total_due,
                         COALESCE(aa.misc_total_due, 0::numeric)              as misc_total_due,
                         COALESCE(aa.subsistence_due, 0::numeric)             as subsistence_due,
                         COALESCE(aa.smart_card_due, 0::numeric)              as smart_card_due,

                         COALESCE(aa.public_transport_total_paid, 0::numeric) as public_transport_total_paid,
                         COALESCE(aa.hired_vehicle_total_paid, 0::numeric)    as hired_vehicle_total_paid,
                         COALESCE(aa.motorcycle_total_paid, 0::numeric)       as motorcycle_total_paid,
                         COALESCE(aa.car_total_paid, 0::numeric)              as car_total_paid,
                         COALESCE(aa.pedal_cycle_total_paid, 0::numeric)      as pedal_cycle_total_paid,
                         COALESCE(aa.parking_total_paid, 0::numeric)          as parking_total_paid,
                         COALESCE(aa.loss_of_earnings_paid, 0::numeric)       as loss_of_earnings_paid,
                         COALESCE(aa.childcare_total_paid, 0::numeric)        as childcare_total_paid,
                         COALESCE(aa.misc_total_paid, 0::numeric)             as misc_total_paid,
                         COALESCE(aa.subsistence_paid, 0::numeric)            as subsistence_paid,
                         COALESCE(aa.smart_card_paid, 0::numeric)             as smart_card_paid

                  from juror_mod.financial_audit_details fad
                           join juror_mod.financial_audit_details_appearances fada
                                on fada.financial_audit_id = fad.id and fada.loc_code = fad.loc_code
                           join juror_mod.appearance_audit aa
                                on aa.loc_code = fada.loc_code and aa.attendance_date = fada.attendance_date and
                                   aa.juror_number = fad.juror_number
                                    and aa."version" = fada.appearance_version) a2) a3) a4;
;

CREATE OR REPLACE VIEW juror_mod.low_level_financial_audit_details_including_approved_amounts
as


select llfad2.*,

       public_transport_total_approved +
       hired_vehicle_total_approved +
       motorcycle_total_approved +
       car_total_approved +
       pedal_cycle_total_approved +
       parking_total_approved +
       loss_of_earnings_approved +
       childcare_total_approved +
       misc_total_approved +
       subsistence_approved +
       smart_card_approved    as total_approved,


       public_transport_total_approved +
       hired_vehicle_total_approved +
       motorcycle_total_approved +
       car_total_approved +
       pedal_cycle_total_approved +
       parking_total_approved as total_travel_approved,

       loss_of_earnings_approved +
       childcare_total_approved +
       misc_total_approved    as total_financial_loss_approved,

       subsistence_approved   as total_subsistence_approved,
       smart_card_approved    as total_smartcard_approved


from (select llfad.public_transport_total_paid -
             COALESCE(lastApprovedllfad.public_transport_total_paid, 0::numeric) as public_transport_total_approved,
             llfad.hired_vehicle_total_paid -
             COALESCE(lastApprovedllfad.hired_vehicle_total_paid, 0::numeric)    as hired_vehicle_total_approved,
             llfad.motorcycle_total_paid -
             COALESCE(lastApprovedllfad.motorcycle_total_paid, 0::numeric)       as motorcycle_total_approved,
             llfad.car_total_paid -
             COALESCE(lastApprovedllfad.car_total_paid, 0::numeric)              as car_total_approved,
             llfad.pedal_cycle_total_paid -
             COALESCE(lastApprovedllfad.pedal_cycle_total_paid, 0::numeric)      as pedal_cycle_total_approved,
             llfad.parking_total_paid -
             COALESCE(lastApprovedllfad.parking_total_paid, 0::numeric)          as parking_total_approved,
             llfad.loss_of_earnings_paid -
             COALESCE(lastApprovedllfad.loss_of_earnings_paid, 0::numeric)       as loss_of_earnings_approved,
             llfad.childcare_total_paid -
             COALESCE(lastApprovedllfad.childcare_total_paid, 0::numeric)        as childcare_total_approved,
             llfad.misc_total_paid -
             COALESCE(lastApprovedllfad.misc_total_paid, 0::numeric)             as misc_total_approved,
             llfad.subsistence_paid -
             COALESCE(lastApprovedllfad.subsistence_paid, 0::numeric)            as subsistence_approved,

             (CASE
                  WHEN llfad.type in ('REAPPROVED_CASH', 'REAPPROVED_BACS')
                      THEN COALESCE(lastApprovedllfad.smart_card_paid, 0::numeric)
                      - llfad.smart_card_paid
                  ELSE
                      llfad.smart_card_paid - COALESCE(lastApprovedllfad.smart_card_paid, 0::numeric)
                 END
                 )                                                               as smart_card_approved,

             llfad.*
      from juror_mod.low_level_financial_audit_details as llfad
               left join juror_mod.low_level_financial_audit_details lastApprovedllfad
                         on lastApprovedllfad.id = llfad.last_approved_faudit
                             and lastApprovedllfad.loc_code = llfad.loc_code
                             and lastApprovedllfad.attendance_date = llfad.attendance_date
                             and llfad.type in (
                                                'REAPPROVED_BACS',
                                                'REAPPROVED_CASH'
                                 )) llfad2;