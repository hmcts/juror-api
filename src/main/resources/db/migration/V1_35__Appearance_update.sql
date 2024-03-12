ALTER TABLE juror_mod.appearance
    ALTER COLUMN pay_cash SET DEFAULT false,
    ALTER COLUMN pay_cash SET NOT NULL,
    ALTER COLUMN is_draft_expense SET DEFAULT true,
    ALTER COLUMN is_draft_expense SET NOT NULL,
    DROP CONSTRAINT appearance_stage_val,
    ADD CONSTRAINT appearance_stage_val CHECK (((appearance_stage)::text = ANY ((ARRAY [
        'CHECKED_IN'::character varying,
        'CHECKED_OUT'::character varying,
        'EXPENSE_ENTERED'::character varying,
        'EXPENSE_ENTERED'::character varying,
        'EXPENSE_AUTHORISED'::character varying,
        'EXPENSE_EDITED'::character varying])::text[])));


INSERT INTO juror_mod.expense_rates(rate_per_mile_car_0_passengers, rate_per_mile_car_1_passengers,
                                    rate_per_mile_car_2_or_more_passengers, rate_per_mile_motorcycle_0_passengers,
                                    rate_per_mile_motorcycle_1_or_more_passengers, rate_per_mile_bike,
                                    limit_financial_loss_half_day, limit_financial_loss_full_day,
                                    limit_financial_loss_half_day_long_trial, limit_financial_loss_full_day_long_trial,
                                    rate_subsistence_standard, rate_subsistence_long_day, rates_effective_from)
values (0.314, 0.356, 0.398, 0.314, 0.324, 0.096, 32.47, 64.95, 64.95, 129.91, 5.71, 12.17, current_date);


DROP VIEW juror_mod.juror_expense_totals;
DROP VIEW juror_mod.juror_expense_subtotals;

CREATE VIEW juror_mod.juror_expense_subtotals
AS SELECT a.juror_number,
          a.pool_number,
          j.first_name,
          j.last_name,
          a.loc_code,
          sum(COALESCE(a.public_transport_total_due, 0::numeric)) AS public_transport_total_due_total,
          sum(COALESCE(a.public_transport_total_paid, 0::numeric)) AS public_transport_total_paid_total,
          sum(COALESCE(a.hired_vehicle_total_due, 0::numeric)) AS hired_vehicle_total_due_total,
          sum(COALESCE(a.hired_vehicle_total_paid, 0::numeric)) AS hired_vehicle_total_paid_total,
          sum(COALESCE(a.motorcycle_total_due, 0::numeric)) AS motorcycle_total_due_total,
          sum(COALESCE(a.motorcycle_total_paid, 0::numeric)) AS motorcycle_total_paid_total,
          sum(COALESCE(a.car_total_due, 0::numeric)) AS car_total_due_total,
          sum(COALESCE(a.car_total_paid, 0::numeric)) AS car_total_paid_total,
          sum(COALESCE(a.pedal_cycle_total_due, 0::numeric)) AS pedal_cycle_total_due_total,
          sum(COALESCE(a.pedal_cycle_total_paid, 0::numeric)) AS pedal_cycle_total_paid_total,
          sum(COALESCE(a.parking_total_due, 0::numeric)) AS parking_total_due_total,
          sum(COALESCE(a.parking_total_paid, 0::numeric)) AS parking_total_paid_total,
          sum(COALESCE(a.loss_of_earnings_due, 0::numeric)) AS loss_of_earnings_due_total,
          sum(COALESCE(a.loss_of_earnings_paid, 0::numeric)) AS loss_of_earnings_paid_total,
          sum(COALESCE(a.childcare_total_due, 0::numeric)) AS childcare_total_due_total,
          sum(COALESCE(a.childcare_total_paid, 0::numeric)) AS childcare_total_paid_total,
          sum(COALESCE(a.misc_total_due, 0::numeric)) AS misc_total_due_total,
          sum(COALESCE(a.misc_total_paid, 0::numeric)) AS misc_total_paid_total,
          sum(COALESCE(a.subsistence_due, 0::numeric)) AS subsistence_due_total,
          sum(COALESCE(a.subsistence_paid, 0::numeric)) AS subsistence_paid_total,
          sum(COALESCE(a.smart_card_due, 0::numeric)) AS smart_card_spend_total,
          sum(CASE a.appearance_stage
              WHEN 'EXPENSE_ENTERED' THEN 1
              WHEN 'EXPENSE_EDITED' THEN 1
              ELSE 0
          END) as pending_approval_count
   FROM juror_mod.appearance a
            JOIN juror_mod.juror j ON j.juror_number::text = a.juror_number::text
   GROUP BY a.juror_number, a.pool_number, j.first_name, j.last_name, a.loc_code;



CREATE VIEW juror_mod.juror_expense_totals
AS SELECT juror_expense_subtotals.juror_number,
          juror_expense_subtotals.pool_number,
          juror_expense_subtotals.first_name,
          juror_expense_subtotals.last_name,
          juror_expense_subtotals.loc_code,
          juror_expense_subtotals.public_transport_total_due_total + juror_expense_subtotals.hired_vehicle_total_due_total + juror_expense_subtotals.motorcycle_total_due_total + juror_expense_subtotals.car_total_due_total + juror_expense_subtotals.pedal_cycle_total_due_total + juror_expense_subtotals.parking_total_due_total AS travel_unapproved,
          juror_expense_subtotals.public_transport_total_paid_total + juror_expense_subtotals.hired_vehicle_total_paid_total + juror_expense_subtotals.motorcycle_total_paid_total + juror_expense_subtotals.car_total_paid_total + juror_expense_subtotals.pedal_cycle_total_paid_total + juror_expense_subtotals.parking_total_paid_total AS travel_approved,
          juror_expense_subtotals.loss_of_earnings_due_total AS financial_loss_unapproved,
          juror_expense_subtotals.loss_of_earnings_paid_total AS financial_loss_approved,
          juror_expense_subtotals.subsistence_due_total AS subsistence_unapproved,
          juror_expense_subtotals.subsistence_paid_total AS subsistence_approved,
          juror_expense_subtotals.smart_card_spend_total,
          juror_expense_subtotals.public_transport_total_due_total + juror_expense_subtotals.hired_vehicle_total_due_total + juror_expense_subtotals.motorcycle_total_due_total + juror_expense_subtotals.car_total_due_total + juror_expense_subtotals.pedal_cycle_total_due_total + juror_expense_subtotals.parking_total_due_total + juror_expense_subtotals.childcare_total_due_total + juror_expense_subtotals.misc_total_due_total + juror_expense_subtotals.loss_of_earnings_due_total + juror_expense_subtotals.subsistence_due_total AS total_unapproved,
          juror_expense_subtotals.public_transport_total_paid_total + juror_expense_subtotals
              .hired_vehicle_total_paid_total + juror_expense_subtotals.motorcycle_total_paid_total + juror_expense_subtotals.car_total_paid_total + juror_expense_subtotals.pedal_cycle_total_paid_total + juror_expense_subtotals.parking_total_paid_total + juror_expense_subtotals.childcare_total_paid_total + juror_expense_subtotals.misc_total_paid_total + juror_expense_subtotals.loss_of_earnings_paid_total + juror_expense_subtotals.subsistence_paid_total AS total_approved,
        juror_expense_subtotals.pending_approval_count
   FROM juror_mod.juror_expense_subtotals;



SELECT setval('juror_mod.rev_info_seq', 100, true);


INSERT INTO juror_mod.rev_info (revision_number, revision_timestamp)
VALUES (nextval('juror_mod.rev_info_seq'), EXTRACT(EPOCH FROM current_date));


INSERT INTO juror_mod.court_location_audit (revision, rev_type, loc_code, public_transport_soft_limit, taxi_soft_limit)
SELECT currval('juror_mod.rev_info_seq'),
       0,
       loc_code,
       public_transport_soft_limit,
       taxi_soft_limit
FROM juror_mod.court_location;
