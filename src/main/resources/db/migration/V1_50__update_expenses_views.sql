-- drop views for updates
drop view  juror_mod.juror_expense_totals;
drop view juror_mod.juror_expense_subtotals;

-- juror_mod.juror_expense_subtotals source
CREATE OR REPLACE VIEW juror_mod.juror_expense_subtotals
AS SELECT a.juror_number,
    a.pool_number,
    j.first_name,
    j.last_name,
    a.loc_code,
    sum(COALESCE(a.public_transport_total_due, 0::numeric)) AS public_transport_total_due_total,
    sum(COALESCE(a.public_transport_total_paid, 0::numeric)) AS public_transport_total_paid_total,
    sum(COALESCE(a.public_transport_total_due, 0::numeric)) - sum(COALESCE(a.public_transport_total_paid, 0::numeric)) as public_transport_total_unpaid,
    sum(COALESCE(a.hired_vehicle_total_due, 0::numeric)) AS hired_vehicle_total_due_total,
    sum(COALESCE(a.hired_vehicle_total_paid, 0::numeric)) AS hired_vehicle_total_paid_total,
    sum(COALESCE(a.hired_vehicle_total_due, 0::numeric)) - sum(COALESCE(a.hired_vehicle_total_paid, 0::numeric)) as hired_vehicle_total_unpaid,
    sum(COALESCE(a.motorcycle_total_due, 0::numeric)) AS motorcycle_total_due_total,
    sum(COALESCE(a.motorcycle_total_paid, 0::numeric)) AS motorcycle_total_paid_total,
    sum(COALESCE(a.motorcycle_total_due, 0::numeric)) - sum(COALESCE(a.motorcycle_total_paid, 0::numeric)) as motorcycle_total_unpaid,
    sum(COALESCE(a.car_total_due, 0::numeric)) AS car_total_due_total,
    sum(COALESCE(a.car_total_paid, 0::numeric)) AS car_total_paid_total,
    sum(COALESCE(a.car_total_due, 0::numeric)) - sum(COALESCE(a.car_total_paid, 0::numeric)) as car_total_unpaid,
    sum(COALESCE(a.pedal_cycle_total_due, 0::numeric)) AS pedal_cycle_total_due_total,
    sum(COALESCE(a.pedal_cycle_total_paid, 0::numeric)) AS pedal_cycle_total_paid_total,
    sum(COALESCE(a.pedal_cycle_total_due, 0::numeric)) - sum(COALESCE(a.pedal_cycle_total_paid, 0::numeric)) as pedal_cycle_total_unpaid,
    sum(COALESCE(a.parking_total_due, 0::numeric)) AS parking_total_due_total,
    sum(COALESCE(a.parking_total_paid, 0::numeric)) AS parking_total_paid_total,
    sum(COALESCE(a.parking_total_due, 0::numeric)) -  sum(COALESCE(a.parking_total_paid, 0::numeric)) as parking_total_unpaid,
    sum(COALESCE(a.loss_of_earnings_due, 0::numeric)) AS loss_of_earnings_due_total,
    sum(COALESCE(a.loss_of_earnings_paid, 0::numeric)) AS loss_of_earnings_paid_total,
    sum(COALESCE(a.loss_of_earnings_due, 0::numeric)) -  sum(COALESCE(a.loss_of_earnings_paid, 0::numeric)) as loss_of_earnings_total_unpaid,
    sum(COALESCE(a.childcare_total_due, 0::numeric)) AS childcare_total_due_total,
    sum(COALESCE(a.childcare_total_paid, 0::numeric)) AS childcare_total_paid_total,
    sum(COALESCE(a.childcare_total_due, 0::numeric)) - sum(COALESCE(a.childcare_total_paid, 0::numeric)) as childcare_total_unpaid,
    sum(COALESCE(a.misc_total_due, 0::numeric)) AS misc_total_due_total,
    sum(COALESCE(a.misc_total_paid, 0::numeric)) AS misc_total_paid_total,
    sum(COALESCE(a.misc_total_due, 0::numeric)) -  sum(COALESCE(a.misc_total_paid, 0::numeric)) as misc_total_unpaid,
    sum(COALESCE(a.subsistence_due, 0::numeric)) AS subsistence_due_total,
    sum(COALESCE(a.subsistence_paid, 0::numeric)) AS subsistence_paid_total,
    sum(COALESCE(a.subsistence_due, 0::numeric)) -  sum(COALESCE(a.subsistence_paid, 0::numeric)) as subsistence_total_unpaid,
    sum(COALESCE(a.smart_card_due, 0::numeric)) AS smart_card_spend_total,
    sum(
        CASE a.appearance_stage
            WHEN 'EXPENSE_ENTERED'::text THEN 1
            WHEN 'EXPENSE_EDITED'::text THEN 1
            ELSE 0
        END) AS pending_approval_count
   FROM juror_mod.appearance a
     JOIN juror_mod.juror j ON j.juror_number::text = a.juror_number::text
  GROUP BY a.juror_number, a.pool_number, j.first_name, j.last_name, a.loc_code;



-- juror_mod.juror_expense_totals source

CREATE OR REPLACE VIEW juror_mod.juror_expense_totals
AS SELECT jes.juror_number,
    jes.pool_number,
    jes.first_name,
    jes.last_name,
    jes.loc_code,
    jes.public_transport_total_due_total + jes.hired_vehicle_total_due_total + jes.motorcycle_total_due_total + jes.car_total_due_total + jes.pedal_cycle_total_due_total + jes.parking_total_due_total AS travel_unapproved,
    jes.public_transport_total_paid_total + jes.hired_vehicle_total_paid_total + jes.motorcycle_total_paid_total + jes.car_total_paid_total + jes.pedal_cycle_total_paid_total + jes.parking_total_paid_total AS travel_approved,
    jes.loss_of_earnings_due_total AS financial_loss_unapproved,
    jes.loss_of_earnings_paid_total AS financial_loss_approved,
    jes.subsistence_due_total AS subsistence_unapproved,
    jes.subsistence_paid_total AS subsistence_approved,
    jes.smart_card_spend_total,
    jes.public_transport_total_unpaid + jes.hired_vehicle_total_unpaid + jes.motorcycle_total_unpaid + jes.car_total_unpaid + jes.pedal_cycle_total_unpaid + jes.parking_total_unpaid + jes.childcare_total_unpaid + jes.misc_total_unpaid + jes.loss_of_earnings_total_unpaid + jes.subsistence_total_unpaid AS total_unapproved,
    jes.public_transport_total_paid_total + jes.hired_vehicle_total_paid_total + jes.motorcycle_total_paid_total + jes.car_total_paid_total + jes.pedal_cycle_total_paid_total + jes.parking_total_paid_total + jes.childcare_total_paid_total + jes.misc_total_paid_total + jes.loss_of_earnings_paid_total + jes.subsistence_paid_total AS total_approved,
    jes.pending_approval_count
   FROM juror_mod.juror_expense_subtotals jes;

