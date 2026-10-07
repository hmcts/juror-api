CREATE OR REPLACE VIEW juror_mod.low_level_financial_audit_details
AS
SELECT a4.*,
       a4.total_due - a4.total_paid AS total_outstanding
FROM (SELECT a3.*,
             a3.total_travel_due - a3.total_travel_paid                 AS total_travel_outstanding,
             a3.total_financial_loss_due - a3.total_financial_loss_paid AS total_financial_loss_outstanding,
             a3.total_subsistence_due - a3.total_subsistence_paid       AS total_subsistence_outstanding,
             CASE
                 WHEN (a3.total_smart_card_paid - a3.total_smart_card_due) >= 0
                     THEN a3.total_smart_card_paid - a3.total_smart_card_due
                 ELSE a3.total_smart_card_due
                 END                                                    AS total_smartcard_outstanding,
             (a3.total_travel_due + a3.total_financial_loss_due + a3.total_subsistence_due) -
             a3.total_smart_card_due                                    AS total_due,
             (a3.total_travel_paid + a3.total_financial_loss_paid + a3.total_subsistence_paid) -
             a3.total_smart_card_paid                                   AS total_paid

      FROM (SELECT a2.*,
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

            FROM (SELECT fad.*,
                         fad.created_on::timestamp::date                      AS created_on_date,
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

                         COALESCE(aa.public_transport_total_due, 0::numeric)  AS public_transport_total_due,
                         COALESCE(aa.hired_vehicle_total_due, 0::numeric)     AS hired_vehicle_total_due,
                         COALESCE(aa.motorcycle_total_due, 0::numeric)        AS motorcycle_total_due,
                         COALESCE(aa.car_total_due, 0::numeric)               AS car_total_due,
                         COALESCE(aa.pedal_cycle_total_due, 0::numeric)       AS pedal_cycle_total_due,
                         COALESCE(aa.parking_total_due, 0::numeric)           AS parking_total_due,
                         COALESCE(aa.loss_of_earnings_due, 0::numeric)        AS loss_of_earnings_due,
                         COALESCE(aa.childcare_total_due, 0::numeric)         AS childcare_total_due,
                         COALESCE(aa.misc_total_due, 0::numeric)              AS misc_total_due,
                         COALESCE(aa.subsistence_due, 0::numeric)             AS subsistence_due,
                         COALESCE(aa.smart_card_due, 0::numeric)              AS smart_card_due,

                         COALESCE(aa.public_transport_total_paid, 0::numeric) AS public_transport_total_paid,
                         COALESCE(aa.hired_vehicle_total_paid, 0::numeric)    AS hired_vehicle_total_paid,
                         COALESCE(aa.motorcycle_total_paid, 0::numeric)       AS motorcycle_total_paid,
                         COALESCE(aa.car_total_paid, 0::numeric)              AS car_total_paid,
                         COALESCE(aa.pedal_cycle_total_paid, 0::numeric)      AS pedal_cycle_total_paid,
                         COALESCE(aa.parking_total_paid, 0::numeric)          AS parking_total_paid,
                         COALESCE(aa.loss_of_earnings_paid, 0::numeric)       AS loss_of_earnings_paid,
                         COALESCE(aa.childcare_total_paid, 0::numeric)        AS childcare_total_paid,
                         COALESCE(aa.misc_total_paid, 0::numeric)             AS misc_total_paid,
                         COALESCE(aa.subsistence_paid, 0::numeric)            AS subsistence_paid,
                         COALESCE(aa.smart_card_paid, 0::numeric)             AS smart_card_paid

                  FROM juror_mod.financial_audit_details fad
                           JOIN juror_mod.financial_audit_details_appearances fada
                                ON fada.financial_audit_id = fad.id
                                    AND fada.loc_code = fad.loc_code
                           JOIN juror_mod.appearance_audit aa
                                ON aa.loc_code = fada.loc_code
                                    AND aa.attendance_date = fada.attendance_date
                                    AND aa.juror_number = fad.juror_number
                                    AND aa."version" = fada.appearance_version
                                    AND aa.f_audit = fad.id) a2) a3) a4;
